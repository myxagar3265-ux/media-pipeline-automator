package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URI;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enterprise Media Distribution Pipeline
 * Architected to handle multi-user OAuth2 credential acquisition and concurrent batch video uploading.
 */
public class YouTubeUploaderStudio extends JFrame {
    // UI Interface Components
    private JTextField txtFolderPath;
    private JTextArea txtGeminiInput;
    private JButton btnSelectFolder, btnUpload, btnLinkAccount;
    private JPanel progressContainer;
    private JScrollPane progressScroll;
    private JComboBox<YouTubeChannel> comboChannels;

    // Asynchronous Task Control Layers
    private ExecutorService uploadExecutor;
    private HttpServer OAuthServer;

    // Design System / Cyber Aesthetics
    private final Color bgDark = new Color(10, 11, 16);
    private final Color panelDark = new Color(20, 22, 30);
    private final Color cyberNeon = new Color(0, 255, 160);
    private final Color cyberPink = new Color(255, 0, 130);
    private final Color textWhite = new Color(230, 240, 255);

    // Initial State Context Settings
    private String globalClientId = "YOUR_CLIENT_ID.apps.googleusercontent.com";
    private String globalClientSecret = "YOUR_CLIENT_SECRET";
    private final int REDIRECT_PORT = 8080;

    /**
     * Decentralized Channel Profile Storage Architecture
     */
    private static class YouTubeChannel {
        String name;
        String clientId;
        String clientSecret;
        String refreshToken;

        public YouTubeChannel(String name, String clientId, String clientSecret, String refreshToken) {
            this.name = name;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.refreshToken = refreshToken;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public YouTubeUploaderStudio() {
        super("🚀 Enterprise Media Distribution Pipeline (OAuth2 Automated)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 850);
        getContentPane().setBackground(bgDark);
        setLayout(new BorderLayout(15, 15));
        setLocationRelativeTo(null);

        // Scan filesystem for global integration keys
        loadGlobalAppSecrets();

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(bgDark);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Section 0: Profile Selector Interface
        JPanel channelPanel = createSection(" 0. USER PROFILE MANAGER / TARGET CHANNELS ");
        channelPanel.setLayout(new BorderLayout(10, 0));
        comboChannels = new JComboBox<>();
        comboChannels.setBackground(panelDark);
        comboChannels.setForeground(cyberNeon);
        comboChannels.setFont(new Font("SansSerif", Font.BOLD, 12));
        loadChannels();

        btnLinkAccount = new JButton("🔗 LINK NEW ACCOUNT");
        btnLinkAccount.setBackground(panelDark);
        btnLinkAccount.setForeground(cyberPink);
        btnLinkAccount.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnLinkAccount.addActionListener(e -> startOAuthAuthorizationFlow());

        channelPanel.add(comboChannels, BorderLayout.CENTER);
        channelPanel.add(btnLinkAccount, BorderLayout.EAST);

        // Section 1: File Storage Location
        JPanel folderPanel = createSection(" 1. SOURCE MEDIA DIRECTORY ");
        folderPanel.setLayout(new BorderLayout(10, 0));
        txtFolderPath = new JTextField();
        txtFolderPath.setBackground(panelDark);
        txtFolderPath.setForeground(textWhite);
        txtFolderPath.setEditable(false);

        btnSelectFolder = new JButton("📁 BROWSE FOLDER");
        btnSelectFolder.setBackground(panelDark);
        btnSelectFolder.setForeground(cyberNeon);
        btnSelectFolder.addActionListener(e -> selectFolder());
        folderPanel.add(txtFolderPath, BorderLayout.CENTER);
        folderPanel.add(btnSelectFolder, BorderLayout.EAST);

        // Section 2: Data Package Ingestion Node
        JPanel geminiPanel = createSection(" 2. INGEST AUTOMATION DATA (BATCH JSON ARRAY) ");
        geminiPanel.setLayout(new BorderLayout());
        txtGeminiInput = new JTextArea(12, 30);
        txtGeminiInput.setBackground(panelDark);
        txtGeminiInput.setForeground(cyberNeon);
        txtGeminiInput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtGeminiInput.setText("[\n" +
                "  {\n" +
                "    \"videoFile\": \"video_sample.mp4\",\n" +
                "    \"title\": \"Automated Scale Deployment Demo\",\n" +
                "    \"tags\": [\"automation\", \"api\", \"java\"],\n" +
                "    \"description\": \"Automated bulk deployment pipeline via Java Content Service.\",\n" +
                "    \"publishAt\": \"2026-07-01T12:00:00+03:00\"\n" +
                "  }\n" +
                "]");
        geminiPanel.add(new JScrollPane(txtGeminiInput), BorderLayout.CENTER);

        // Section 3: Thread Status Monitoring Array
        JPanel progressSection = createSection(" 3. CONCURRENT THREAD EXECUTION POOL ");
        progressSection.setLayout(new BorderLayout());
        progressSection.setPreferredSize(new Dimension(100, 180));

        progressContainer = new JPanel();
        progressContainer.setBackground(panelDark);
        progressContainer.setLayout(new BoxLayout(progressContainer, BoxLayout.Y_AXIS));

        progressScroll = new JScrollPane(progressContainer);
        progressScroll.setBorder(null);
        progressSection.add(progressScroll, BorderLayout.CENTER);

        // Engine Initiation Controller
        btnUpload = new JButton("⚡ EXECUTE PARALLEL UPLOAD PIPELINE");
        btnUpload.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnUpload.setBackground(cyberPink);
        btnUpload.setForeground(Color.WHITE);
        btnUpload.setPreferredSize(new Dimension(100, 55));
        btnUpload.addActionListener(e -> startMultiThreadedPipeline());

        mainPanel.add(channelPanel); mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(folderPanel); mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(geminiPanel); mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(progressSection);

        add(mainPanel, BorderLayout.CENTER);
        add(btnUpload, BorderLayout.SOUTH);
    }

    private void loadGlobalAppSecrets() {
        File secretFile = new File("client_secrets.txt");
        if (!secretFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(secretFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("CLIENT_ID=")) globalClientId = line.split("=")[1].trim();
                if (line.startsWith("CLIENT_SECRET=")) globalClientSecret = line.split("=")[1].trim();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveGlobalAppSecrets(String clientId, String clientSecret) {
        try (PrintWriter writer = new PrintWriter(new File("client_secrets.txt"))) {
            writer.println("CLIENT_ID=" + clientId.trim());
            writer.println("CLIENT_SECRET=" + clientSecret.trim());
            this.globalClientId = clientId.trim();
            this.globalClientSecret = clientSecret.trim();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadChannels() {
        comboChannels.removeAllItems();
        File file = new File("channels.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    comboChannels.addItem(new YouTubeChannel(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim()));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * 🔐 AUTOMATED OAUTH2 FLOW WITH GRAPHICAL INITIAL SETUP
     * Spawns an internal micro-server to capture auth redirection tokens asynchronously.
     */
    private void startOAuthAuthorizationFlow() {
        if (globalClientId.contains("YOUR_CLIENT_ID") || globalClientSecret.contains("YOUR_CLIENT_SECRET")) {
            JTextField idField = new JTextField(30);
            JTextField secretField = new JTextField(30);
            JPanel setupPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            setupPanel.add(new JLabel("Paste Client ID from Google Cloud Console:"));
            setupPanel.add(idField);
            setupPanel.add(new JLabel("Paste Client Secret from Google Cloud Console:"));
            setupPanel.add(secretField);

            int result = JOptionPane.showConfirmDialog(this, setupPanel, "OAuth 2.0 API Initialization Setup", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION && !idField.getText().trim().isEmpty() && !secretField.getText().trim().isEmpty()) {
                saveGlobalAppSecrets(idField.getText(), secretField.getText());
            } else {
                return;
            }
        }

        String profileName = JOptionPane.showInputDialog(this, "Enter a short alias/profile name for this channel mapping:", "Profile Identifier Assignment", JOptionPane.QUESTION_MESSAGE);
        if (profileName == null || profileName.trim().isEmpty()) return;

        try {
            // Allocate internal server socket for redirection loop interception
            if (OAuthServer != null) OAuthServer.stop(0);
            OAuthServer = HttpServer.create(new InetSocketAddress(REDIRECT_PORT), 0);

            OAuthServer.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String query = exchange.getRequestURI().getQuery();
                    String code = "";
                    if (query != null && query.contains("code=")) {
                        for (String param : query.split("&")) {
                            if (param.startsWith("code=")) {
                                code = param.split("=")[1];
                                break;
                            }
                        }
                    }

                    String responseText = "<h1>Authorization Successful!</h1><p>You can close this tab and return to the application.</p>";
                    exchange.sendResponseHeaders(200, responseText.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseText.getBytes());
                    os.close();

                    if (!code.isEmpty()) {
                        final String authCode = code;
                        new Thread(() -> exchangeCodeForRefreshToken(authCode, profileName)).start();
                    }
                    OAuthServer.stop(1);
                }
            });
            OAuthServer.start();

            // Construct secure handshaking URL string
            String redirectUri = "http://localhost:" + REDIRECT_PORT;
            String authUrl = "https://accounts.google.com/o/oauth2/auth"
                    + "?client_id=" + globalClientId
                    + "&redirect_uri=" + redirectUri
                    + "&response_type=code"
                    + "&scope=https://www.googleapis.com/auth/youtube.upload"
                    + "&access_type=offline"
                    + "&prompt=consent";

            Desktop.getDesktop().browse(new URI(authUrl));
            JOptionPane.showMessageDialog(this, "Please authorize access inside your system web browser tab...", "OAuth Authorization Requested", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Internal Callback Server Allocation Error: " + ex.getMessage(), "OAuth Stack Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Executes HTTP POST block exchange trading transient code for persistent refresh_token.
     */
    private void exchangeCodeForRefreshToken(String code, String profileName) {
        try {
            String redirectUri = "http://localhost:" + REDIRECT_PORT;
            HttpClient client = HttpClient.newHttpClient();
            String params = "code=" + code
                    + "&client_id=" + globalClientId
                    + "&client_secret=" + globalClientSecret
                    + "&redirect_uri=" + redirectUri
                    + "&grant_type=authorization_code";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (responseBody.contains("refresh_token\"")) {
                String refreshToken = parseJsonStringField(responseBody, "refresh_token");

                try (FileWriter fw = new FileWriter("channels.txt", true);
                     PrintWriter pw = new PrintWriter(fw)) {
                    pw.println(profileName + ";" + globalClientId + ";" + globalClientSecret + ";" + refreshToken);
                }

                SwingUtilities.invokeLater(() -> {
                    loadChannels();
                    JOptionPane.showMessageDialog(this, "Channel Profile '" + profileName + "' successfully linked and stored!", "Profile Registered", JOptionPane.INFORMATION_MESSAGE);
                });
            } else {
                throw new Exception("Google Endpoint Contract Rejection: " + responseBody);
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Token Exchange Failure: " + e.getMessage(), "Security Error", JOptionPane.ERROR_MESSAGE));
        }
    }

    private String parseJsonStringField(String json, String field) {
        int start = json.indexOf("\"" + field + "\"");
        int colon = json.indexOf(":", start);
        int firstQuote = json.indexOf("\"", colon);
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        return json.substring(firstQuote + 1, secondQuote).trim();
    }

    private JPanel createSection(String title) {
        JPanel p = new JPanel();
        p.setBackground(bgDark);
        TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(cyberNeon, 1), title);
        b.setTitleColor(cyberNeon);
        p.setBorder(b);
        return p;
    }

    private void selectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtFolderPath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Instantiates the concurrent processing loop via throttled Executor Service pool.
     */
    private void startMultiThreadedPipeline() {
        String folderPath = txtFolderPath.getText();
        String jsonInput = txtGeminiInput.getText().trim();
        YouTubeChannel selectedChannel = (YouTubeChannel) comboChannels.getSelectedItem();

        if (folderPath.isEmpty() || selectedChannel == null) {
            JOptionPane.showMessageDialog(this, "Validation Failed. Verify structural media source paths and target channel settings.", "Configuration Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        progressContainer.removeAll();
        setUIEnabled(false);

        // Throttle runtime execution limit to 2 concurrent worker nodes
        if (uploadExecutor != null && !uploadExecutor.isShutdown()) {
            uploadExecutor.shutdownNow();
        }
        uploadExecutor = Executors.newFixedThreadPool(2);

        new Thread(() -> {
            try {
                // Dynamically rotate transient access token context
                String accessToken = getAccessToken(selectedChannel);
                ArrayList<String> videoObjects = splitJsonArray(jsonInput);

                if (videoObjects.isEmpty()) {
                    throw new Exception("Structural structural malformation detected within payload.");
                }

                for (String obj : videoObjects) {
                    String rawFile = parseJsonField(obj, "videoFile");
                    String title = parseJsonField(obj, "title");
                    String desc = parseJsonField(obj, "description");
                    String publishAt = parseJsonField(obj, "publishAt");
                    String tagsRaw = parseJsonField(obj, "tags");

                    File fileToUpload = new File(folderPath, rawFile);
                    if (!fileToUpload.exists()) continue;

                    JProgressBar pBar = new JProgressBar();
                    pBar.setBackground(bgDark);
                    pBar.setForeground(cyberNeon);
                    pBar.setStringPainted(true);
                    pBar.setString(rawFile + " ➔ Queued...");
                    pBar.setIndeterminate(true);

                    SwingUtilities.invokeLater(() -> {
                        progressContainer.add(pBar);
                        progressContainer.add(Box.createVerticalStrut(5));
                        progressContainer.revalidate();
                        progressContainer.repaint();
                    });

                    // Deploy processing tasks into isolation thread blocks
                    uploadExecutor.submit(() -> uploadSingleVideoTask(fileToUpload, title, desc, publishAt, tagsRaw, accessToken, pBar));
                }
                uploadExecutor.shutdown();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Pipeline runtime processing collapse: " + ex.getMessage(), "Pipeline Error", JOptionPane.ERROR_MESSAGE);
                SwingUtilities.invokeLater(() -> setUIEnabled(true));
            }
        }).start();
    }

    /**
     * Executes localized filename sanitization, encodes parameters into binary streams, and dispatches a multipart POST request.
     */
    private void uploadSingleVideoTask(File file, String title, String description, String publishAt, String tagsRaw, String accessToken, JProgressBar pBar) {
        File renamedFile = file;
        try {
            SwingUtilities.invokeLater(() -> pBar.setString("🔄 Sanitizing IO Node: " + file.getName()));
            String safeTitle = title.replaceAll("[^a-zA-Z0-9а-яА-ЯіІїЇєЄґҐ_ ]", "");
            String ext = file.getName().substring(file.getName().lastIndexOf("."));
            renamedFile = new File(file.getParent(), safeTitle + ext);

            if (!file.getAbsolutePath().equals(renamedFile.getAbsolutePath())) {
                Files.move(file.toPath(), renamedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            SwingUtilities.invokeLater(() -> pBar.setString("📡 Streaming via REST Client..."));

            String[] tags = tagsRaw.replaceAll("[\\[\\]\"]", "").split(",");
            StringBuilder tagsJson = new StringBuilder("[");
            for (int i = 0; i < tags.length; i++) {
                String t = tags[i].trim();
                if (!t.isEmpty()) {
                    tagsJson.append("\"").append(t).append("\"");
                    if (i < tags.length - 1) tagsJson.append(",");
                }
            }
            tagsJson.append("]");

            String metadataJson = "{"
                    + "\"snippet\":{"
                    + "    \"title\":\"" + title + "\","
                    + "    \"description\":\"" + description + "\","
                    + "    \"tags\":" + tagsJson.toString() + ","
                    + "    \"categoryId\":\"22\","
                    + "    \"defaultLanguage\":\"en\""
                    + "},"
                    + "\"status\":{"
                    + "    \"privacyStatus\":\"private\","
                    + "    \"publishAt\":\"" + publishAt + "\","
                    + "    \"selfDeclaredMadeForKids\":false"
                    + "}"
                    + "}";

            // Assemble raw multipart/related request payload byte array maps manually
            String boundary = "JavaBatchScheduler" + System.currentTimeMillis();
            ByteArrayOutputStream uploadBody = new ByteArrayOutputStream();
            uploadBody.write(("--" + boundary + "\r\n").getBytes());
            uploadBody.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".getBytes());
            uploadBody.write((metadataJson + "\r\n").getBytes());

            uploadBody.write(("--" + boundary + "\r\n").getBytes());
            uploadBody.write(("Content-Disposition: form-data; name=\"video\"; filename=\"" + renamedFile.getName() + "\"\r\n").getBytes());
            uploadBody.write("Content-Type: video/mp4\r\n\r\n".getBytes());

            byte[] videoBytes = Files.readAllBytes(renamedFile.toPath());
            uploadBody.write(videoBytes);
            uploadBody.write(("\r\n--" + boundary + "--\r\n").getBytes());

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest uploadRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/upload/youtube/v3/videos?part=snippet,status"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "multipart/related; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(uploadBody.toByteArray()))
                    .build();

            HttpResponse<String> response = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                SwingUtilities.invokeLater(() -> {
                    pBar.setIndeterminate(false);
                    pBar.setValue(100);
                    pBar.setString("✅ PARSED & DEPLOYED: " + title);
                });
            } else {
                throw new Exception("HTTP Endpoint returned status: " + response.statusCode());
            }
        } catch (Exception e) {
            final String errName = renamedFile.getName();
            SwingUtilities.invokeLater(() -> {
                pBar.setIndeterminate(false);
                pBar.setBackground(Color.RED);
                pBar.setString("❌ PROCESSING REJECTED: " + errName);
            });
        } finally {
            checkPipelineCompletion();
        }
    }

    /**
     * Rotates security contexts trading permanent refresh token variables for transient hourly authorization codes.
     */
    private String getAccessToken(YouTubeChannel channel) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String tokenParams = "client_id=" + channel.clientId
                + "&client_secret=" + channel.clientSecret
                + "&refresh_token=" + channel.refreshToken
                + "&grant_type=refresh_token";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(tokenParams)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String text = response.body();

        if (text.contains("access_token\"")) {
            return parseJsonStringField(text, "access_token");
        }
        throw new Exception("Context lifecycle maintenance rotation failed.");
    }

    private ArrayList<String> splitJsonArray(String json) {
        ArrayList<String> list = new ArrayList<>();
        int index = 0;
        while ((index = json.indexOf("{", index)) != -1) {
            int end = json.indexOf("}", index);
            if (end == -1) break;
            list.add(json.substring(index, end + 1));
            index = end + 1;
        }
        return list;
    }

    private String parseJsonField(String json, String field) {
        try {
            int start = json.indexOf("\"" + field + "\"");
            if (start == -1) return "";
            int colon = json.indexOf(":", start);
            if (field.equals("tags")) {
                int open = json.indexOf("[", colon);
                int close = json.indexOf("]", open);
                return json.substring(open, close + 1);
            } else {
                int first = json.indexOf("\"", colon);
                int second = json.indexOf("\"", first + 1);
                return json.substring(first + 1, second).trim();
            }
        } catch (Exception e) { return ""; }
    }

    private synchronized void checkPipelineCompletion() {
        boolean allDone = true;
        for (Component c : progressContainer.getComponents()) {
            if (c instanceof JProgressBar) {
                JProgressBar b = (JProgressBar) c;
                if (b.isIndeterminate() || (b.getValue() < 100 && !b.getString().startsWith("❌"))) {
                    allDone = false;
                    break;
                }
            }
        }
        if (allDone) SwingUtilities.invokeLater(() -> setUIEnabled(true));
    }

    private void setUIEnabled(boolean enabled) {
        btnSelectFolder.setEnabled(enabled);
        btnUpload.setEnabled(enabled);
        btnLinkAccount.setEnabled(enabled);
        txtGeminiInput.setEditable(enabled);
        comboChannels.setEnabled(enabled);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new YouTubeUploaderStudio().setVisible(true));
    }
}