// ========== ChatServer.java ==========
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Multithreaded Chat Server
 * Handles multiple client connections using socket programming and threading
 * Features: User registration, broadcast messaging, private messaging, user list
 */
public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static Map<String, ClientHandler> clientMap = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("=== MULTITHREADED CHAT SERVER ===");
        System.out.println("Server starting on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for connections...");
            System.out.println("Commands: /users, /private <username> <message>, /quit");
            System.out.println("==========================================");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                
                System.out.println("[" + getCurrentTime() + "] New client connected from: " + 
                                 clientSocket.getInetAddress() + ". Total clients: " + clients.size());
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    // Broadcast message to all connected clients
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender && client.getUsername() != null) {
                client.sendMessage(message);
            }
        }
    }
    
    // Send private message to specific user
    public static void sendPrivateMessage(String targetUsername, String message, ClientHandler sender) {
        ClientHandler targetClient = clientMap.get(targetUsername);
        if (targetClient != null) {
            targetClient.sendMessage("[PRIVATE from " + sender.getUsername() + "]: " + message);
            sender.sendMessage("[PRIVATE to " + targetUsername + "]: " + message);
        } else {
            sender.sendMessage("❌ User '" + targetUsername + "' not found or offline.");
        }
    }
    
    // Remove client when disconnected
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        if (client.getUsername() != null) {
            clientMap.remove(client.getUsername());
            broadcastMessage("🚪 " + client.getUsername() + " left the chat", null);
        }
        System.out.println("[" + getCurrentTime() + "] Client disconnected. Total clients: " + clients.size());
    }
    
    // Add client to username mapping
    public static void addClientMapping(String username, ClientHandler client) {
        clientMap.put(username, client);
    }
    
    // Get list of online users
    public static String getOnlineUsers() {
        if (clientMap.isEmpty()) {
            return "📭 No users online.";
        }
        return "👥 Online users (" + clientMap.size() + "): " + String.join(", ", clientMap.keySet());
    }
    
    // Check if username is already taken
    public static boolean isUsernameTaken(String username) {
        return clientMap.containsKey(username);
    }
    
    // Get current timestamp
    private static String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}

// ========== ClientHandler.java ==========
/**
 * Handles individual client connections
 * Manages user authentication, message processing, and client communication
 */
class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            // Welcome message and username setup
            sendMessage("🎉 Welcome to the Multithreaded Chat Server!");
            sendMessage("📝 Please enter your username:");
            
            // Get username from client
            while (username == null) {
                String inputUsername = input.readLine();
                if (inputUsername == null || inputUsername.trim().isEmpty()) {
                    sendMessage("❌ Username cannot be empty. Please enter a valid username:");
                    continue;
                }
                
                inputUsername = inputUsername.trim();
                if (ChatServer.isUsernameTaken(inputUsername)) {
                    sendMessage("❌ Username '" + inputUsername + "' is already taken. Please choose another:");
                    continue;
                }
                
                username = inputUsername;
                ChatServer.addClientMapping(username, this);
                
                // Notify all users about new user
                sendMessage("✅ Welcome, " + username + "! You have joined the chat.");
                sendMessage("💡 Commands: /users (list users), /private <username> <message>, /quit");
                sendMessage("==========================================");
                
                ChatServer.broadcastMessage("👋 " + username + " joined the chat!", this);
                
                System.out.println("[" + getCurrentTime() + "] User '" + username + "' joined the chat");
            }
            
            // Handle messages from client
            String message;
            while ((message = input.readLine()) != null) {
                processMessage(message);
            }
            
        } catch (IOException e) {
            System.out.println("[" + getCurrentTime() + "] Client connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    // Process incoming messages and commands
    private void processMessage(String message) {
        if (message.trim().isEmpty()) {
            return;
        }
        
        // Handle commands
        if (message.startsWith("/")) {
            handleCommand(message);
        } else {
            // Broadcast regular message
            String formattedMessage = "[" + getCurrentTime() + "] " + username + ": " + message;
            ChatServer.broadcastMessage(formattedMessage, this);
            System.out.println(formattedMessage);
        }
    }
    
    // Handle chat commands
    private void handleCommand(String command) {
        String[] parts = command.split(" ", 3);
        String cmd = parts[0].toLowerCase();
        
        switch (cmd) {
            case "/users":
                sendMessage(ChatServer.getOnlineUsers());
                break;
                
            case "/private":
                if (parts.length < 3) {
                    sendMessage("❌ Usage: /private <username> <message>");
                } else {
                    String targetUser = parts[1];
                    String privateMessage = parts[2];
                    ChatServer.sendPrivateMessage(targetUser, privateMessage, this);
                }
                break;
                
            case "/quit":
                sendMessage("👋 Goodbye, " + username + "!");
                cleanup();
                break;
                
            case "/help":
                sendMessage("📋 Available commands:");
                sendMessage("  /users - Show online users");
                sendMessage("  /private <username> <message> - Send private message");
                sendMessage("  /quit - Leave the chat");
                sendMessage("  /help - Show this help message");
                break;
                
            default:
                sendMessage("❌ Unknown command: " + cmd + ". Type /help for available commands.");
                break;
        }
    }
    
    // Send message to this client
    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }
    
    // Get username
    public String getUsername() {
        return username;
    }
    
    // Cleanup resources when client disconnects
    private void cleanup() {
        try {
            ChatServer.removeClient(this);
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
    
    // Get current timestamp
    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}

/*
 * The ChatClient class must be moved to a separate file named ChatClient.java.
 * Copy the ChatClient code below into ChatClient.java:
 */

// ========== ChatClient.java ==========
/*
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;
    
    public ChatClient() {
        scanner = new Scanner(System.in);
    }
    
    public void start() {
        try {
            System.out.println("🔗 Connecting to chat server...");
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("✅ Connected to server!");
            
            Thread messageListener = new Thread(this::listenForMessages);
            messageListener.setDaemon(true);
            messageListener.start();
            
            handleUserInput();
            
        } catch (IOException e) {
            System.err.println("❌ Connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.out.println("❌ Connection to server lost.");
        }
    }
    
    private void handleUserInput() {
        System.out.println("💬 You can now start chatting! Type /help for commands.");
        
        while (scanner.hasNextLine()) {
            String userInput = scanner.nextLine();
            
            if (userInput.equalsIgnoreCase("/quit")) {
                break;
            }
            
            if (output != null) {
                output.println(userInput);
            }
        }
    }
    
    private void cleanup() {
        try {
            System.out.println("👋 Disconnecting from server...");
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            scanner.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== CHAT CLIENT ===");
        ChatClient client = new ChatClient();
        client.start();
    }
}
*/

// ========== How to Run the Application ==========
/*
COMPILATION AND EXECUTION INSTRUCTIONS:

1. Split the classes into separate files:
   - ChatServer.java (contains ChatServer and ClientHandler)
   - ChatClient.java (contains ChatClient)

2. Compile all Java files:
   javac *.java

3. Start the Server (in one terminal):
   java ChatServer

4. Start Multiple Clients (in separate terminals):
   java ChatClient

FEATURES IMPLEMENTED:
✅ Multithreaded server handling multiple clients simultaneously
✅ Real-time messaging between all connected users
✅ Private messaging system (/private command)
✅ User list functionality (/users command)
✅ Proper username validation and duplicate prevention
✅ Graceful connection handling and cleanup
✅ Timestamped messages
✅ Command system with help functionality
✅ Thread-safe operations using ConcurrentHashMap
✅ Professional error handling and logging

COMMANDS AVAILABLE:
- /users : Show list of online users
- /private <username> <message> : Send private message
- /quit : Leave the chat
- /help : Show available commands

ARCHITECTURE:
- Server: Handles multiple client connections using threading
- ClientHandler: Manages individual client sessions
- Client: Provides user interface and server communication
- Thread-safe collections for managing concurrent access
*/
