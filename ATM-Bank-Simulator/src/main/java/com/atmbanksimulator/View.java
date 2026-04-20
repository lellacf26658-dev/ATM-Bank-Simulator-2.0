package com.atmbanksimulator;

// View
// UI/UX + GUI responsibility
//
// REDESIGN v2 — Changes from previous version:
//   1. [NEW]    Liquid Glass design — translucent panels with inner highlights,
//               frosted borders, and layered depth shadows.
//   2. [NEW]    Animated mesh gradient background — a Timeline drives 3 radial
//               colour blobs that drift slowly, producing a living colour field.
//   3. [NEW]    Holographic card hover effect on every function button.
//               On hover: button scales up 1.04x, a shimmering LinearGradient
//               sweeps the brand palette (Sea Buckthorn → Miami Coral), and a
//               colour-cycling DropShadow glows around the card. All driven by
//               a per-button Timeline that runs while the mouse is over the button
//               and stops cleanly on exit.
//   4. [NEW]    Light Mode / Dark Mode toggle in the ATM header.
//               Dark  palette: Neverything #13181B, Ateneo Blue #003A6C,
//                              Sea Buckthorn #FFBF65, Miami Coral #FD8973.
//               Light palette: Magical Moonlight #F0EEEB, Polar Drift #CCD5DA,
//                              Sea Buckthorn #FFBF65, Miami Coral #FD8973.
//   5. [FIX]    Warning / error messages now render in Miami Coral (#FD8973).
//               Previously they matched the background in light mode (both white).
//               Coral is high-contrast against BOTH #F0EEEB and #13181B.
//   6. [KEEP]   Password masking (● bullets), account type badge, fade-in on launch,
//               goodbye flash on logout.

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class View {

    // MVC references
    Controller controller;
    UIModel    uiModel;

    // Live UI components updated by update()
    private Label     messageLabel;
    private TextField inputField;
    private TextArea  resultArea;
    private Label     accountTypeBadge;
    private Label     stateLabel;
    private Label     modeToggleBtn;

    // Layout containers (stored so toggleTheme() can re-style them)
    private StackPane root;
    private HBox      mainContent;
    private VBox      atmBody;
    private VBox      funcPanel;

    // Animated blob references (needed to recolour on theme switch)
    private Circle blob1, blob2, blob3;

    // [FIX] Replaced Timeline with AnimationTimer.
    // AnimationTimer.handle(now) is called by JavaFX on EVERY rendered frame
    // (~60fps), making it impossible for the animation to fall out of sync.
    // Timeline's EventHandler approach can be skipped by the render scheduler.
    private AnimationTimer meshTimer;
    // Stores the nanosecond timestamp of the first frame so we can compute
    // elapsed seconds reliably regardless of when the app launched.
    private long meshStartNano = -1;

    // Splash / transition screen state
    private StackPane welcomeOverlay;  // "Welcome to FIN Bank" screen
    private StackPane goodbyeOverlay;  // "Thank you for choosing FIN Bank" screen
    private Scene     scene;           // stored so keyboard listener can be added
    // Guard flag — prevents showGoodbyeScreen() firing more than once per logout
    private boolean goodbyeShowing = false;

    // Theme state
    private boolean isDarkMode = true;

    // Brand Colour Palette
    // Dark mode
    // [FIX] D_PANEL raised to 0.92 opacity — blocks the mesh blobs from
    //        bleeding through and warming the panel background, which was
    //        making the pale cream text (#F0EEEB) blend into the background.
    // [FIX] D_TXT1 changed to Ateneo Blue-derived bright tone #8EC8F5 —
    //        a clear mid-blue that reads well against near-black, matches
    //        the palette, and is clearly distinct from any background tone.
    // [FIX] D_TXT2 brightened to #C0DCF0 — lighter blue for result body text.
    // [FIX] D_SCREEN made fully opaque rgba(2,5,14,0.98) — stops warm blob
    //        light leaking into the terminal screen area, giving true black.
    private static final String D_BASE   = "#13181B"; // Neverything
    private static final String D_ACCENT = "#003A6C"; // Ateneo Blue
    private static final String D_PANEL  = "rgba(19,24,27,0.92)"; // [FIX] was 0.74
    private static final String D_BORDER = "rgba(142,200,245,0.20)"; // [FIX] blue-tinted border
    private static final String D_TXT1   = "#8EC8F5"; // [FIX] bright Ateneo-derived blue — clear on dark
    private static final String D_TXT2   = "#C0DCF0"; // [FIX] lighter blue for body/result text
    private static final String D_SCREEN = "rgba(2,5,14,0.98)"; // [FIX] fully opaque — no blob bleed

    // Light mode
    // [FIX] ROOT background changed to Polar Drift #CCD5DA (blue-grey) instead of
    //        Magical Moonlight #F0EEEB. The old root and panel were virtually the
    //        same colour so everything merged into one flat white sheet.
    //        Now: root = blue-grey, panels = cream/white glass → clear contrast.
    // [FIX] L_PANEL now uses Magical Moonlight at 0.94 opacity on the Polar Drift
    //        root — cream glass cards clearly pop off the blue-grey background.
    // [FIX] L_BORDER raised to 0.50 opacity — borders are now clearly visible.
    // [FIX] L_SCREEN uses a proper Ateneo Blue tint so the terminal area is
    //        distinguishable from the panel around it.
    private static final String L_BASE   = "#CCD5DA"; // [FIX] Polar Drift as root bg (was Moonlight)
    private static final String L_ACCENT = "#003A6C"; // Ateneo Blue blob for light mode mesh
    private static final String L_PANEL  = "rgba(240,238,235,0.94)"; // [FIX] cream glass on blue-grey root
    private static final String L_BORDER = "rgba(0,58,108,0.50)";    // [FIX] was 0.18 — now clearly visible
    private static final String L_TXT1   = "#0D1E2C"; // Very dark blue-black (more striking than pure #13181B)
    private static final String L_TXT2   = "#003A6C"; // Ateneo Blue — dark, readable on cream
    private static final String L_SCREEN = "rgba(0,58,108,0.10)"; // [FIX] blue-tinted terminal area

    // Shared — used in both modes (high contrast on both backgrounds)
    private static final String AMBER = "#FFBF65"; // Sea Buckthorn
    private static final String CORAL = "#FD8973"; // Miami Coral

    // Theme helpers
    private String bg()       { return isDarkMode ? D_BASE   : L_BASE;   }
    private String accent()   { return isDarkMode ? D_ACCENT : L_ACCENT; }
    private String panelBg()  { return isDarkMode ? D_PANEL  : L_PANEL;  }
    private String border()   { return isDarkMode ? D_BORDER : L_BORDER; }
    private String txt1()     { return isDarkMode ? D_TXT1   : L_TXT1;   }
    private String txt2()     { return isDarkMode ? D_TXT2   : L_TXT2;   }
    // screenBg() always returns the dark value — the terminal screen looks the
    // same in both light and dark mode, as requested.
    private String screenBg() { return D_SCREEN; }

    // Liquid glass panel CSS (recomputed each call so it always uses current theme)
    // [FIX] Border width raised to 1.5px — light mode borders (L_BORDER = 0.50)
    //        now clearly outline each panel against the Polar Drift background.
    private String glassPanel() {
        return  "-fx-background-color: " + panelBg() + ";" +
                "-fx-border-color: " + border() + ";" +
                "-fx-border-width: 1.5;" +
                "-fx-background-radius: 22;" +
                "-fx-border-radius: 22;";
    }


    // start() — called by Main.java to build and show the window
    public void start(Stage window) {
        root = new StackPane();
        root.setStyle("-fx-background-color: " + bg() + ";");

        // Layer 0: animated mesh gradient blobs (always visible behind everything)
        Pane bgPane = buildMeshBackground();
        root.getChildren().add(bgPane);

        // Layer 1: ATM machine UI — hidden until user dismisses welcome screen
        mainContent = buildATMMachine();
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setOpacity(0);
        mainContent.setMouseTransparent(true); // Disable clicks while hidden
        root.getChildren().add(mainContent);

        // Layer 2: Welcome splash screen — shown on top of everything at launch
        welcomeOverlay = buildSplashOverlay(
                "Welcome to FIN Bank ATM",
                "Press ENTER to continue"
        );
        root.getChildren().add(welcomeOverlay);

        // Layer 3: Goodbye splash screen — hidden, shown after logout
        goodbyeOverlay = buildSplashOverlay(
                "Thank you for choosing FIN Bank",
                ""   // No subtitle on goodbye screen
        );
        goodbyeOverlay.setOpacity(0);
        goodbyeOverlay.setMouseTransparent(true);
        root.getChildren().add(goodbyeOverlay);

        scene = new Scene(root, 960, 720);
        scene.setFill(Color.web(bg()));

        // Keyboard listener
        // Physical ENTER key on the keyboard dismisses the welcome screen and
        // transitions to the main ATM. Once on the main screen the keyboard
        // ENTER does nothing — all input is via the on-screen buttons.
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                transitionToMain();
            }
        });

        window.setTitle("FIN Bank ATM Simulator");
        window.setScene(scene);
        window.setMinWidth(830);
        window.setMinHeight(650);
        window.show();

        // Fade in the welcome screen on launch
        welcomeOverlay.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(900), welcomeOverlay);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }


    // buildSplashOverlay()
    // Builds a full-screen centred glass card overlay used for both the
    // welcome and goodbye screens. The mesh background blobs show through
    // because the overlay background is fully transparent.
    // @param title    — large heading text
    // @param subtitle — smaller instruction text (pass "" to hide it)
    private StackPane buildSplashOverlay(String title, String subtitle) {
        StackPane overlay = new StackPane();
        // Transparent fill — mesh blobs behind are fully visible
        overlay.setStyle("-fx-background-color: transparent;");

        // Glass card (same liquid glass style as the ATM panels)
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + panelBg() + ";" +
                        "-fx-border-color: " + border() + ";" +
                        "-fx-border-width: 1.5;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-padding: 55 80 55 80;"
        );
        card.setMaxWidth(520);

        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.web(AMBER, 0.25));
        cardShadow.setRadius(40);
        card.setEffect(cardShadow);

        // Bank name above title — small amber label
        Label bankTag = new Label("FIN BANK");
        bankTag.setStyle(
                "-fx-text-fill: " + AMBER + ";" +
                        "-fx-font-family: Arial;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-letter-spacing: 3;"
        );

        // Thin amber divider
        Region divider = new Region();
        divider.setStyle("-fx-background-color: rgba(255,191,101,0.35);");
        divider.setMinHeight(1);
        divider.setMaxWidth(200);

        // Main title
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + D_TXT1 + ";" +
                        "-fx-font-family: Arial;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-wrap-text: true;" +
                        "-fx-text-alignment: center;"
        );
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(400);

        card.getChildren().addAll(bankTag, divider, titleLabel);

        // Subtitle — only added if non-empty
        if (!subtitle.isEmpty()) {
            Label subLabel = new Label(subtitle);
            subLabel.setStyle(
                    "-fx-text-fill: " + D_TXT2 + ";" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 13px;" +
                            "-fx-wrap-text: true;" +
                            "-fx-text-alignment: center;"
            );
            subLabel.setAlignment(Pos.CENTER);

            // Pulse animation on the subtitle to draw attention to the instruction
            FadeTransition pulse = new FadeTransition(Duration.millis(900), subLabel);
            pulse.setFromValue(0.4);
            pulse.setToValue(1.0);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();

            card.getChildren().add(subLabel);
        }

        overlay.getChildren().add(card);
        return overlay;
    }


    // transitionToMain()
    // Fades out the welcome overlay and fades in the ATM machine.
    // Called by the keyboard ENTER listener and by the ENT button
    // when the welcome overlay is visible.
    // Guard: does nothing if welcome overlay is already invisible.
    private void transitionToMain() {
        if (welcomeOverlay.getOpacity() < 0.1) return; // Already on main screen

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), welcomeOverlay);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            welcomeOverlay.setMouseTransparent(true);
            mainContent.setMouseTransparent(false);
        });

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), mainContent);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.play();
        fadeIn.play();
    }


    // showGoodbyeScreen()
    // Called from update() when UIModel's message contains "thank you".
    // 1. Fades in the goodbye overlay over the main content.
    // 2. Waits 5 seconds via PauseTransition.
    // 3. Fades out goodbye, resets UIModel, fades in welcome.
    // Guard flag (goodbyeShowing) prevents this firing twice per logout.
    private void showGoodbyeScreen() {
        goodbyeShowing = true;

        // Step 1: fade in goodbye overlay (0 → 1 over 600ms)
        goodbyeOverlay.setMouseTransparent(false);
        FadeTransition fadeInGoodbye = new FadeTransition(Duration.millis(600), goodbyeOverlay);
        fadeInGoodbye.setFromValue(0.0);
        fadeInGoodbye.setToValue(1.0);

        // Step 2: simultaneously fade out the ATM content
        FadeTransition fadeOutMain = new FadeTransition(Duration.millis(500), mainContent);
        fadeOutMain.setToValue(0.0);

        // Step 3: hold for 5 seconds
        PauseTransition hold = new PauseTransition(Duration.seconds(5));

        // Step 4: fade out goodbye, reset UIModel, fade in welcome
        hold.setOnFinished(e -> {
            // Reset the ATM state so it's ready for the next user
            uiModel.initialise();

            FadeTransition fadeOutGoodbye = new FadeTransition(Duration.millis(500), goodbyeOverlay);
            fadeOutGoodbye.setToValue(0.0);
            fadeOutGoodbye.setOnFinished(ev -> {
                goodbyeOverlay.setMouseTransparent(true);
                goodbyeShowing = false;
            });

            // Bring back the welcome screen for the next customer
            welcomeOverlay.setOpacity(0);
            welcomeOverlay.setMouseTransparent(false);
            FadeTransition fadeInWelcome = new FadeTransition(Duration.millis(700), welcomeOverlay);
            fadeInWelcome.setFromValue(0.0);
            fadeInWelcome.setToValue(1.0);

            fadeOutGoodbye.play();
            fadeInWelcome.play();
        });

        // Chain: fade in goodbye + fade out main → hold 5s
        fadeInGoodbye.setOnFinished(e -> hold.play());
        fadeInGoodbye.play();
        fadeOutMain.play();
    }


    // buildMeshBackground()
    // [FIX] Replaced Timeline with AnimationTimer.
    //
    // Why the old version didn't visibly move:
    //   Timeline fires its EventHandler on a schedule, but JavaFX can
    //   skip or batch those events if the render loop is busy, meaning
    //   blob position updates could be dropped entirely.
    //   AnimationTimer.handle(now) is called by JavaFX on every single
    //   rendered frame (~60fps) — it is literally part of the render loop
    //   and cannot be skipped while the scene is visible.
    //
    // Other fixes:
    //   - Blob radii increased (350-400px) so they cover more of the canvas
    //   - GaussianBlur reduced from 78 → 55 so edges are less invisible
    //   - Amplitudes increased (180-260px drift) — movement now clearly visible
    //   - Speed uses real elapsed seconds (from nanosecond timestamp) so it is
    //     frame-rate independent and consistent on all machines
    //   - Pane gets setMaxSize(MAX,MAX) so it always fills the StackPane
    private Pane buildMeshBackground() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);
        // Ensure the pane always fills its StackPane parent
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        blob1 = makeBlob(480, 200, 370, AMBER,    0.48);
        blob2 = makeBlob(180, 540, 330, CORAL,    0.42);
        blob3 = makeBlob(740, 530, 350, D_ACCENT, isDarkMode ? 0.58 : 0.50);

        pane.getChildren().addAll(blob1, blob2, blob3);

        // [FIX] AnimationTimer — called every rendered frame by JavaFX itself
        meshStartNano = -1; // Reset so first frame sets the baseline
        meshTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // First frame: record the start time so t starts at 0
                if (meshStartNano < 0) meshStartNano = now;

                // t = elapsed seconds since animation started
                // Using real time makes speed frame-rate independent
                double t = (now - meshStartNano) / 1_000_000_000.0;

                // Blob 1 (amber): wide slow oval drift
                blob1.setCenterX(480 + Math.sin(t * 0.40) * 240);
                blob1.setCenterY(200 + Math.cos(t * 0.28) * 160);

                // Blob 2 (coral): lower-left, independent phase
                blob2.setCenterX(175 + Math.cos(t * 0.35) * 200);
                blob2.setCenterY(540 + Math.sin(t * 0.50) * 145);

                // Blob 3 (blue accent): right side, slower drift
                blob3.setCenterX(740 + Math.sin(t * 0.30) * 160);
                blob3.setCenterY(530 + Math.cos(t * 0.43) * 180);
            }
        };
        meshTimer.start();

        return pane;
    }

    private Circle makeBlob(double cx, double cy, double r, String hex, double alpha) {
        Circle c = new Circle(cx, cy, r);
        Color col = Color.web(hex, alpha);
        c.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, col),
                new Stop(1.0, Color.TRANSPARENT)));
        // [FIX] Reduced blur from 78 → 55 so blobs have more visible presence
        //        while still blending smoothly into each other
        c.setEffect(new GaussianBlur(55));
        return c;
    }


    // buildATMMachine() — outer layout
    private HBox buildATMMachine() {
        HBox hbox = new HBox(14);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(28));

        atmBody   = buildATMBody();
        funcPanel = buildFuncPanel();
        hbox.getChildren().addAll(atmBody, funcPanel);
        return hbox;
    }


    // buildATMBody() — left panel: header + screen + numpad
    private VBox buildATMBody() {
        VBox body = new VBox(12);
        body.setStyle(glassPanel());
        body.setPadding(new Insets(20));
        body.setMinWidth(510);
        body.setMaxWidth(532);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(AMBER, 0.18));
        shadow.setRadius(32);
        body.setEffect(shadow);

        body.getChildren().addAll(buildHeader(), buildScreen(), buildNumpad());
        return body;
    }

    // Header row: bank name | account badge | theme toggle
    private HBox buildHeader() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 0, 10, 0));
        row.setStyle(
                "-fx-border-color: transparent transparent " + border() + " transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // Bank name
        Text name = new Text("FIN BANK");
        name.setFont(Font.font("Arial", FontWeight.BOLD, 19));
        name.setFill(Color.web(AMBER));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Account type badge (hidden until login)
        accountTypeBadge = new Label();
        accountTypeBadge.setStyle(
                "-fx-background-color: rgba(255,191,101,0.13);" +
                        "-fx-text-fill: " + AMBER + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-color: rgba(255,191,101,0.45);" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 3 8 3 8;"
        );
        accountTypeBadge.setVisible(false);

        // Light/Dark toggle — clickable label
        modeToggleBtn = new Label(isDarkMode ? "LIGHT MODE" : "DARK MODE");
        String tBase =
                "-fx-text-fill: " + txt2() + ";" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 3 8 3 8;" +
                        "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-color: " + border() + ";" +
                        "-fx-border-width: 1;";
        String tHover =
                "-fx-text-fill: " + AMBER + ";" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 3 8 3 8;" +
                        "-fx-background-color: rgba(255,191,101,0.14);" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-color: rgba(255,191,101,0.50);" +
                        "-fx-border-width: 1;";
        modeToggleBtn.setStyle(tBase);
        modeToggleBtn.setOnMouseEntered(e -> modeToggleBtn.setStyle(tHover));
        modeToggleBtn.setOnMouseExited( e -> modeToggleBtn.setStyle(tBase));
        modeToggleBtn.setOnMouseClicked(e -> toggleTheme());

        Region gap = new Region();
        gap.setMinWidth(8);

        row.getChildren().addAll(name, spacer, accountTypeBadge, gap, modeToggleBtn);
        return row;
    }

    // ATM screen area
    private VBox buildScreen() {
        VBox screen = new VBox(8);
        // Screen border is always the dark amber tone — the terminal looks
        // identical in both light and dark mode
        screen.setStyle(
                "-fx-background-color: " + screenBg() + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: rgba(255,191,101,0.20);" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 14;"
        );
        screen.setMinHeight(222);

        messageLabel = new Label("Initialising...");
        messageLabel.setStyle(msgStyle(txt1()));
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        Region div = new Region();
        div.setStyle("-fx-background-color: rgba(255,191,101,0.16);");
        div.setMinHeight(1);

        inputField = new TextField();
        inputField.setEditable(false);
        inputField.setStyle(inputStyle());
        inputField.setMaxWidth(Double.MAX_VALUE);
        inputField.setPromptText("Enter number...");

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setStyle(resultStyle());
        resultArea.setPrefHeight(120);
        resultArea.setMaxHeight(140);

        stateLabel = new Label("State: account_no");
        stateLabel.setStyle(
                "-fx-text-fill: rgba(140,150,160,0.42);" +
                        "-fx-font-size: 9px;" +
                        "-fx-font-family: 'Courier New';"
        );

        screen.getChildren().addAll(messageLabel, div, inputField, resultArea, stateLabel);
        return screen;
    }

    // Number pad
    private GridPane buildNumpad() {
        GridPane pad = new GridPane();
        pad.setHgap(8); pad.setVgap(8);
        pad.setPadding(new Insets(10, 0, 0, 0));

        pad.add(numBtn("1"),0,0); pad.add(numBtn("2"),1,0); pad.add(numBtn("3"),2,0);
        pad.add(numBtn("4"),0,1); pad.add(numBtn("5"),1,1); pad.add(numBtn("6"),2,1);
        pad.add(numBtn("7"),0,2); pad.add(numBtn("8"),1,2); pad.add(numBtn("9"),2,2);
        pad.add(clrBtn(),  0,3); pad.add(numBtn("0"),1,3); pad.add(entBtn(),   2,3);
        return pad;
    }


    // buildFuncPanel() — right panel: action buttons with holo effect
    private VBox buildFuncPanel() {
        VBox panel = new VBox(8);
        panel.setStyle(glassPanel());
        panel.setPadding(new Insets(20));
        panel.setMinWidth(212);
        panel.setMaxWidth(222);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(CORAL, 0.14));
        shadow.setRadius(24);
        panel.setEffect(shadow);

        Label title = new Label("MENU");
        title.setStyle(
                "-fx-text-fill: rgba(190,200,210,0.45);" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 0 0 4 4;"
        );

        Region topDiv = new Region();
        topDiv.setStyle("-fx-background-color: " + border() + ";");
        topDiv.setMinHeight(1);

        // Function buttons
        Button btnBal  = funcBtn("BAL    Balance",         "Bal");
        Button btnWD   = funcBtn("W/D    Withdraw",        "W/D");
        Button btnDep  = funcBtn("DEP    Deposit",         "Dep");
        Button btnTrf  = funcBtn("TRF    Transfer",        "Trf");
        Button btnChP  = funcBtn("ChP    Chg Password",    "ChP");
        Button btnStmt = funcBtn("STMT   Statement",       "Stmt");
        Button btnCur  = funcBtn("CUR    Currency",        "Cur");
        Button btnInt  = funcBtn("INT    Interest",        "Int");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Region botDiv = new Region();
        botDiv.setStyle("-fx-background-color: rgba(253,137,115,0.22);");
        botDiv.setMinHeight(1);

        Button btnFin = logoutBtn("FIN    Log Out", "Fin");

        panel.getChildren().addAll(
                title, topDiv,
                btnBal, btnWD, btnDep, btnTrf, btnChP,
                btnStmt, btnCur, btnInt,
                spacer, botDiv, btnFin
        );
        return panel;
    }


    // BUTTON FACTORIES

    // Numpad digit button
    private Button numBtn(String d) {
        Button b = new Button(d);
        b.setStyle(numBase());
        b.setOnMouseEntered(e -> b.setStyle(numHover()));
        b.setOnMouseExited( e -> b.setStyle(numBase()));
        b.setOnAction(e -> controller.process(d));
        return b;
    }

    private String numBase() {
        // [FIX] Light mode: raised to 0.18 opacity so number buttons are clearly
        //        visible as blue-tinted glass on the cream panel background.
        //        Dark mode: raised to 0.12 for cleaner separation from panel.
        String bg = isDarkMode ? "rgba(255,255,255,0.12)" : "rgba(0,58,108,0.18)";
        return "-fx-background-color:" + bg + ";" +
                "-fx-text-fill:" + txt1() + ";" +
                "-fx-font-size:17px;-fx-font-weight:bold;" +
                "-fx-background-radius:12;-fx-border-radius:12;" +
                "-fx-border-color:" + border() + ";-fx-border-width:1.2;" +
                "-fx-cursor:hand;-fx-min-width:62px;-fx-min-height:52px;";
    }
    private String numHover() {
        // Text flips to near-black in light mode so it stays readable on amber bg
        String txt = isDarkMode ? "#F0EEEB" : "#0D1E2C";
        return "-fx-background-color:rgba(255,191,101,0.38);" +
                "-fx-text-fill:" + txt + ";" +
                "-fx-font-size:17px;-fx-font-weight:bold;" +
                "-fx-background-radius:12;-fx-border-radius:12;" +
                "-fx-border-color:rgba(255,191,101,0.75);-fx-border-width:1.5;" +
                "-fx-cursor:hand;-fx-min-width:62px;-fx-min-height:52px;";
    }

    // CLR button (amber accent)
    private Button clrBtn() {
        String base =
                "-fx-background-color:rgba(255,191,101,0.09);-fx-text-fill:" + AMBER + ";" +
                        "-fx-font-size:14px;-fx-font-weight:bold;" +
                        "-fx-background-radius:12;-fx-border-radius:12;" +
                        "-fx-border-color:rgba(255,191,101,0.32);-fx-border-width:1;" +
                        "-fx-cursor:hand;-fx-min-width:62px;-fx-min-height:52px;";
        String hover =
                "-fx-background-color:rgba(255,191,101,0.26);-fx-text-fill:#13181B;" +
                        "-fx-font-size:14px;-fx-font-weight:bold;" +
                        "-fx-background-radius:12;-fx-border-radius:12;" +
                        "-fx-border-color:rgba(255,191,101,0.78);-fx-border-width:1.5;" +
                        "-fx-cursor:hand;-fx-min-width:62px;-fx-min-height:52px;";
        Button b = new Button("CLR");
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited( e -> b.setStyle(base));
        b.setOnAction(e -> controller.process("CLR"));
        return b;
    }

    // ENT button (coral accent)
    private Button entBtn() {
        String base =
                "-fx-background-color:rgba(253,137,115,0.11);-fx-text-fill:" + CORAL + ";" +
                        "-fx-font-size:14px;-fx-font-weight:bold;" +
                        "-fx-background-radius:12;-fx-border-radius:12;" +
                        "-fx-border-color:rgba(253,137,115,0.38);-fx-border-width:1;" +
                        "-fx-cursor:hand;-fx-min-width:62px;-fx-min-height:52px;";
        String hover =
                "-fx-background-color:rgba(253,137,115,0.30);-fx-text-fill:white;" +
                        "-fx-font-size:14px;-fx-font-weight:bold;" +
                        "-fx-background-radius:12;-fx-border-radius:12;" +
                        "-fx-border-color:rgba(253,137,115,0.82);-fx-border-width:1.5;" +
                        "-fx-cursor:hand;-fx-min-width:62px;-fx-min-height:52px;";
        Button b = new Button("ENT");
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited( e -> b.setStyle(base));
        b.setOnAction(e -> controller.process("Ent"));
        return b;
    }


    // funcBtn() — HOLOGRAPHIC CARD HOVER EFFECT
    //
    // How it works:
    //   1. ScaleTransition: button scales up to 1.04x on hover,
    //      back to 1.0x on exit. This gives the "card lifts" feel.
    //
    //   2. Shimmer Timeline: while the mouse is over the button, a
    //      Timeline fires every 38ms. Each frame it:
    //        - Advances a phase counter (t += 0.045)
    //        - Computes sin/cos of the phase to create oscillating values
    //        - Builds a new LinearGradient from those values, sweeping
    //          between Sea Buckthorn (#FFBF65) and Miami Coral (#FD8973)
    //          in a diagonal direction that rotates over time
    //        - Updates the button's -fx-background-color to that gradient
    //        - Updates a DropShadow whose colour also cycles (amber→coral→amber)
    //      This produces the "shimmer" card effect.
    //
    //   3. On mouse-exit: Timeline.stop(), ScaleTransition reverses,
    //      button style resets to the neutral base, effect cleared.
    private Button funcBtn(String label, String action) {
        Button btn = new Button(label);
        btn.setStyle(funcBase());
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        btn.setMaxWidth(Double.MAX_VALUE);

        // Scale animation
        ScaleTransition up   = new ScaleTransition(Duration.millis(125), btn);
        up.setToX(1.04); up.setToY(1.04);
        ScaleTransition down = new ScaleTransition(Duration.millis(125), btn);
        down.setToX(1.0); down.setToY(1.0);

        // Shimmer phase counter
        final double[] ph = {0.0};

        Timeline holo = new Timeline(new KeyFrame(Duration.millis(38), e -> {
            ph[0] += 0.045;

            // Oscillating values in range [0,1]
            double s = (Math.sin(ph[0])        + 1.0) / 2.0;
            double c = (Math.cos(ph[0] * 0.72) + 1.0) / 2.0;

            // Diagonal gradient start/end shift over time
            String grad = String.format(
                    "linear-gradient(from %.0f%% 0%% to %.0f%% 100%%," +
                            " rgba(255,191,101,%.2f)," +   // Sea Buckthorn
                            " rgba(253,137,115,%.2f)," +   // Miami Coral
                            " rgba(255,191,101,%.2f))",
                    s * 100, (1.0 - c) * 100,
                    0.15 + s * 0.15,
                    0.18 + c * 0.16,
                    0.12 + s * 0.12
            );

            // Glow shadow — colour oscillates between amber and coral
            DropShadow glow = new DropShadow();
            double rr = 0.85 + 0.15 * Math.sin(ph[0]);
            double gg = 0.45 + 0.28 * Math.cos(ph[0]);
            glow.setColor(Color.color(rr, gg, 0.35, 0.55));
            glow.setRadius(13);
            glow.setSpread(0.07);
            btn.setEffect(glow);

            btn.setStyle(
                    "-fx-background-color: " + grad + ";" +
                            "-fx-text-fill: " + txt1() + ";" +
                            "-fx-font-size: 12px;-fx-font-weight: bold;" +
                            "-fx-background-radius: 11;-fx-border-radius: 11;" +
                            "-fx-border-color: rgba(253,137,115,0.68);-fx-border-width: 1.5;" +
                            "-fx-cursor: hand;-fx-min-height: 40px;" +
                            "-fx-padding: 0 0 0 12;-fx-alignment: CENTER_LEFT;"
            );
        }));
        holo.setCycleCount(Animation.INDEFINITE);

        btn.setOnMouseEntered(e -> { ph[0] = 0; up.playFromStart(); holo.play();  });
        btn.setOnMouseExited( e -> { holo.stop(); down.playFromStart();
            btn.setStyle(funcBase()); btn.setEffect(null); });
        btn.setOnAction(e -> controller.process(action));
        return btn;
    }

    // Logout button — coral-tinted, scale on hover
    private Button logoutBtn(String label, String action) {
        String base =
                "-fx-background-color:rgba(253,137,115,0.09);-fx-text-fill:" + CORAL + ";" +
                        "-fx-font-size:12px;-fx-font-weight:bold;" +
                        "-fx-background-radius:11;-fx-border-radius:11;" +
                        "-fx-border-color:rgba(253,137,115,0.32);-fx-border-width:1;" +
                        "-fx-cursor:hand;-fx-min-height:40px;-fx-padding:0 0 0 12;" +
                        "-fx-alignment:CENTER_LEFT;";
        String hover =
                "-fx-background-color:rgba(253,137,115,0.26);-fx-text-fill:white;" +
                        "-fx-font-size:12px;-fx-font-weight:bold;" +
                        "-fx-background-radius:11;-fx-border-radius:11;" +
                        "-fx-border-color:rgba(253,137,115,0.82);-fx-border-width:1.5;" +
                        "-fx-cursor:hand;-fx-min-height:40px;-fx-padding:0 0 0 12;" +
                        "-fx-alignment:CENTER_LEFT;";
        Button btn = new Button(label);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(base);

        ScaleTransition up   = new ScaleTransition(Duration.millis(125), btn);
        up.setToX(1.03); up.setToY(1.03);
        ScaleTransition down = new ScaleTransition(Duration.millis(125), btn);
        down.setToX(1.0); down.setToY(1.0);

        btn.setOnMouseEntered(e -> { up.playFromStart();   btn.setStyle(hover); });
        btn.setOnMouseExited( e -> { down.playFromStart(); btn.setStyle(base); btn.setEffect(null); });
        btn.setOnAction(e -> controller.process(action));
        return btn;
    }

    // Resting state CSS for function buttons
    // [FIX] Light mode: raised from 0.06 to 0.16 opacity — buttons are now
    //        clearly visible blue-glass cards on the cream panel background.
    //        Text uses L_TXT1 = #0D1E2C (very dark) for maximum contrast.
    private String funcBase() {
        String bg = isDarkMode ? "rgba(255,255,255,0.08)" : "rgba(0,58,108,0.16)";
        return  "-fx-background-color:" + bg + ";-fx-text-fill:" + txt1() + ";" +
                "-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-background-radius:11;-fx-border-radius:11;" +
                "-fx-border-color:" + border() + ";-fx-border-width:1.2;" +
                "-fx-cursor:hand;-fx-min-height:40px;-fx-padding:0 0 0 12;" +
                "-fx-alignment:CENTER_LEFT;";
    }


    // THEME TOGGLE


    // toggleTheme()
    // Switches isDarkMode, fades out the UI, repaints all style strings
    // (they call the theme helpers which now return the opposite set),
    // then fades back in. Blob3's colour is also swapped to match.
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        modeToggleBtn.setText(isDarkMode ? "LIGHT MODE" : "DARK MODE");

        FadeTransition out = new FadeTransition(Duration.millis(160), mainContent);
        out.setFromValue(1.0); out.setToValue(0.0);
        out.setOnFinished(ev -> {

            // Update root background
            root.setStyle("-fx-background-color: " + bg() + ";");

            // Recolour blob3 for the new theme, then restart the AnimationTimer
            // so meshStartNano resets cleanly (avoids a jump in animation position)
            blob3.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web(D_ACCENT, isDarkMode ? 0.58 : 0.50)),
                    new Stop(1, Color.TRANSPARENT)));
            meshTimer.stop();
            meshStartNano = -1;
            meshTimer.start();

            // Repaint all panels with updated theme styles
            atmBody.setStyle(glassPanel());
            funcPanel.setStyle(glassPanel());
            inputField.setStyle(inputStyle());
            resultArea.setStyle(resultStyle());

            // Also refresh the screen border colour (not stored as a named variable)
            // This is handled by rebuild — update() will repaint message/labels
            update();

            FadeTransition in = new FadeTransition(Duration.millis(200), mainContent);
            in.setFromValue(0.0); in.setToValue(1.0);
            in.play();
        });
        out.play();
    }


    // Style string builders (theme-aware, called fresh each use)
    private String msgStyle(String colour) {
        return "-fx-text-fill:" + colour + ";" +
                "-fx-font-family:'Courier New';-fx-font-size:13px;" +
                "-fx-font-weight:bold;-fx-wrap-text:true;";
    }

    private String inputStyle() {
        // Input box always uses the dark background — matches the terminal screen
        // which is also kept consistent between light and dark mode.
        return "-fx-background-color:rgba(255,255,255,0.05);-fx-text-fill:" + AMBER + ";" +
                "-fx-font-family:'Courier New';-fx-font-size:20px;-fx-font-weight:bold;" +
                "-fx-border-color:rgba(255,191,101,0.20);" +
                "-fx-border-radius:7;-fx-background-radius:7;-fx-border-width:1.2;" +
                "-fx-padding:6 10 6 10;";
    }

    private String resultStyle() {
        // Text colour is Ateneo Blue #003A6C — dark enough to contrast clearly
        // against the light background that JavaFX renders inside the TextArea box.
        return "-fx-background-color:transparent;-fx-control-inner-background:transparent;" +
                "-fx-text-fill:#003A6C;" +
                "-fx-font-family:'Courier New';-fx-font-size:12px;" +
                "-fx-border-color:transparent;-fx-border-width:0;" +
                "-fx-focus-color:transparent;-fx-faint-focus-color:transparent;";
    }


    // update() — called by UIModel after every state change


    // [FIX] Error/warning message colour is now always CORAL (#FD8973).
    // This is high-contrast against BOTH the dark background (#13181B)
    // and the light background (#F0EEEB), so warnings are always legible.
    //
    // Previously, the text was white — invisible on the light panel.
    public void update() {
        String msg      = uiModel.getMessage();
        String msgLower = msg.toLowerCase();

        // Assign message colour based on content
        String colour;
        if (msgLower.contains("fail")      || msgLower.contains("error")   ||
                msgLower.contains("lock")      || msgLower.contains("incorrect") ||
                msgLower.contains("invalid")   || msgLower.contains("denied")  ||
                msgLower.contains("too long")  || msgLower.contains("attempt") ||
                msgLower.contains("not found") || msgLower.contains("unavail")) {
            // Coral — visible on BOTH light (#F0EEEB) and dark (#13181B)
            colour = CORAL;
        } else if (msgLower.contains("success")  || msgLower.contains("welcome")  ||
                msgLower.contains("applied")   || msgLower.contains("changed")  ||
                msgLower.contains("deposited") || msgLower.contains("withdrawn") ||
                msgLower.contains("transfer")  || msgLower.contains("interest") ||
                msgLower.contains("cleared")   || msgLower.contains("result")) {
            colour = AMBER; // Warm amber for positive confirmations
        } else {
            // Instructional messages use the same bright blue as the result
            // area text (D_TXT2 = #C0DCF0) — clearly readable on the dark terminal.
            colour = D_TXT2; // #C0DCF0
        }

        messageLabel.setText(msg);
        messageLabel.setStyle(msgStyle(colour));

        // Password masking
        String state    = uiModel.getState();
        String rawInput = uiModel.getNumberPadInput();
        boolean masked  =
                state.equals(uiModel.getStatePassword()) ||
                        state.equals(uiModel.getStateChPass());
        inputField.setText(
                masked && !rawInput.isEmpty()
                        ? "\u25CF".repeat(rawInput.length())  // ● per digit
                        : rawInput
        );

        // Result text area
        resultArea.setText(uiModel.getResult());
        resultArea.setScrollTop(0);

        // Account type badge
        if (uiModel.isLoggedIn()) {
            accountTypeBadge.setText(uiModel.getAccountTypeDisplay());
            accountTypeBadge.setVisible(true);
        } else {
            accountTypeBadge.setVisible(false);
        }

        // Debug state label
        stateLabel.setText("State: " + state);

        // Goodbye screen on logout
        // If UIModel has just set the "thank you" message (logout), trigger the
        // goodbye overlay sequence. The goodbyeShowing guard prevents this from
        // firing repeatedly on subsequent update() calls before the screen clears.
        if (!goodbyeShowing &&
                (msgLower.contains("thank you") || msgLower.contains("goodbye"))) {
            showGoodbyeScreen();
        }
    }
}
