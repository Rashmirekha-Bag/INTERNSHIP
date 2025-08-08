import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Single File AI-Based Recommendation System
 * All classes in one file for easy compilation
 */
public class SingleFileRecommendationSystem {
    
    private static final String DATA_FILE = "user_ratings.csv";
    private static final String PRODUCTS_FILE = "products.csv";
    
    private Map<Integer, Map<Integer, Double>> userRatings;
    private ProductCatalog productCatalog;
    
    public SingleFileRecommendationSystem() {
        this.userRatings = new HashMap<>();
        this.productCatalog = new ProductCatalog();
        initializeSystem();
    }
    
    private void initializeSystem() {
        try {
            System.out.println("🤖 Initializing AI-Based Recommendation System...");
            
            if (!new File(DATA_FILE).exists() || !new File(PRODUCTS_FILE).exists()) {
                System.out.println("📊 Generating sample data...");
                generateSampleData();
            }
            
            productCatalog.loadProducts(PRODUCTS_FILE);
            loadUserRatings();
            
            System.out.println("✅ System initialized successfully!");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadUserRatings() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        int userId = Integer.parseInt(parts[0].trim());
                        int productId = Integer.parseInt(parts[1].trim());
                        double rating = Double.parseDouble(parts[2].trim());
                        
                        userRatings.computeIfAbsent(userId, id -> new HashMap<>())
                                  .put(productId, rating);
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                    }
                }
            }
        }
        System.out.println("✅ Loaded ratings for " + userRatings.size() + " users");
    }
    
    private void generateSampleData() throws IOException {
        generateProductsData();
        generateUserRatingsData();
    }
    
    private void generateProductsData() throws IOException {
        try (FileWriter writer = new FileWriter(PRODUCTS_FILE)) {
            writer.write("ProductID,ProductName,Category,Price\n");
            String[] products = {
                "1,iPhone 15 Pro,Electronics,999.99",
                "2,MacBook Air M2,Electronics,1199.99",
                "3,AirPods Pro,Electronics,249.99",
                "4,Samsung Galaxy S24,Electronics,899.99",
                "5,Sony WH-1000XM5,Electronics,399.99",
                "6,iPad Pro,Electronics,799.99",
                "7,Nintendo Switch,Gaming,299.99",
                "8,PlayStation 5,Gaming,499.99",
                "9,Xbox Series X,Gaming,499.99",
                "10,Steam Deck,Gaming,399.99",
                "11,The Great Gatsby,Books,12.99",
                "12,To Kill a Mockingbird,Books,14.99",
                "13,1984,Books,13.99",
                "14,Pride and Prejudice,Books,11.99",
                "15,The Catcher in the Rye,Books,15.99",
                "16,Nike Air Max,Fashion,129.99",
                "17,Adidas Ultraboost,Fashion,159.99",
                "18,Levi's 501 Jeans,Fashion,89.99",
                "19,Ray-Ban Aviators,Fashion,199.99",
                "20,Apple Watch Series 9,Electronics,399.99"
            };
            for (String product : products) {
                writer.write(product + "\n");
            }
        }
        System.out.println("✅ Generated products catalog");
    }
    
    private void generateUserRatingsData() throws IOException {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            Random random = new Random(42);
            for (int userId = 1; userId <= 50; userId++) {
                int numRatings = 8 + random.nextInt(8);
                Set<Integer> ratedProducts = new HashSet<>();
                
                for (int i = 0; i < numRatings; i++) {
                    int productId = 1 + random.nextInt(20);
                    if (ratedProducts.contains(productId)) {
                        i--;
                        continue;
                    }
                    ratedProducts.add(productId);
                    
                    double rating = generateBiasedRating(userId, productId, random);
                    writer.write(userId + "," + productId + "," + String.format("%.1f", rating) + "\n");
                }
            }
        }
        System.out.println("✅ Generated user ratings data");
    }
    
    private double generateBiasedRating(int userId, int productId, Random random) {
        double baseRating = 2.5 + random.nextGaussian() * 0.8;
        
        if (userId % 3 == 0) {
            if (productId <= 6 || productId == 20) baseRating += 1.0;
        } else if (userId % 3 == 1) {
            if (productId >= 7 && productId <= 10) baseRating += 1.2;
        } else {
            if (productId >= 11 && productId <= 19) baseRating += 0.8;
        }
        
        return Math.max(1.0, Math.min(5.0, baseRating));
    }
    
    private double calculateUserSimilarity(int user1, int user2) {
        Map<Integer, Double> ratings1 = userRatings.get(user1);
        Map<Integer, Double> ratings2 = userRatings.get(user2);
        
        if (ratings1 == null || ratings2 == null) return 0.0;
        
        Set<Integer> commonItems = new HashSet<>(ratings1.keySet());
        commonItems.retainAll(ratings2.keySet());
        
        if (commonItems.size() < 2) return 0.0;
        
        double mean1 = commonItems.stream().mapToDouble(ratings1::get).average().orElse(0.0);
        double mean2 = commonItems.stream().mapToDouble(ratings2::get).average().orElse(0.0);
        
        double numerator = 0.0;
        double sumSquares1 = 0.0;
        double sumSquares2 = 0.0;
        
        for (int item : commonItems) {
            double diff1 = ratings1.get(item) - mean1;
            double diff2 = ratings2.get(item) - mean2;
            
            numerator += diff1 * diff2;
            sumSquares1 += diff1 * diff1;
            sumSquares2 += diff2 * diff2;
        }
        
        double denominator = Math.sqrt(sumSquares1 * sumSquares2);
        return denominator == 0.0 ? 0.0 : numerator / denominator;
    }
    
    public List<Recommendation> getRecommendationsForUser(int userId, int numRecommendations) {
        if (!userRatings.containsKey(userId)) {
            return new ArrayList<>();
        }
        
        Map<Integer, Double> userRatingsMap = userRatings.get(userId);
        Map<Integer, Double> scores = new HashMap<>();
        Map<Integer, Double> similarities = new HashMap<>();
        
        // Find similar users
        for (int otherUser : userRatings.keySet()) {
            if (otherUser == userId) continue;
            
            double similarity = calculateUserSimilarity(userId, otherUser);
            if (similarity > 0.1) {
                similarities.put(otherUser, similarity);
            }
        }
        
        // Calculate recommendation scores
        for (int similarUser : similarities.keySet()) {
            double similarity = similarities.get(similarUser);
            Map<Integer, Double> similarUserRatings = userRatings.get(similarUser);
            
            for (int item : similarUserRatings.keySet()) {
                if (!userRatingsMap.containsKey(item)) {
                    double rating = similarUserRatings.get(item);
                    scores.put(item, scores.getOrDefault(item, 0.0) + similarity * rating);
                }
            }
        }
        
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(numRecommendations)
                .map(entry -> new Recommendation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
    
    public void showUserRatings(int userId) {
        System.out.println("\n📊 User " + userId + "'s Previous Ratings:");
        System.out.println("==========================================");
        
        Map<Integer, Double> ratings = userRatings.get(userId);
        if (ratings == null || ratings.isEmpty()) {
            System.out.println("❌ No ratings found for this user.");
            return;
        }
        
        for (Map.Entry<Integer, Double> entry : ratings.entrySet()) {
            int productId = entry.getKey();
            double rating = entry.getValue();
            Product product = productCatalog.getProduct(productId);
            
            String stars = "";
            for (int i = 0; i < Math.max(0, (int) Math.round(rating)); i++) {
                stars += "⭐";
            }
            
            System.out.printf("• %s - Rating: %.1f/5.0 %s\n",
                    product != null ? product.getName() : "Product ID: " + productId,
                    rating, stars);
        }
    }
    
    public void displayRecommendations(int userId, int numRecommendations) {
        System.out.println("\n🎯 Getting AI-powered recommendations for User " + userId + "...");
        System.out.println("==========================================");
        
        List<Recommendation> recommendations = getRecommendationsForUser(userId, numRecommendations);
        
        if (recommendations.isEmpty()) {
            System.out.println("❌ No recommendations available for this user.");
            return;
        }
        
        System.out.println("🌟 Top " + recommendations.size() + " Recommendations:");
        System.out.println("==========================================");
        
        int rank = 1;
        for (Recommendation rec : recommendations) {
            Product product = productCatalog.getProduct(rec.getProductId());
            double normalizedScore = Math.min(5.0, Math.max(1.0, rec.getScore() / 2.0));
            
            System.out.printf("%d. %s\n", rank++,
                    product != null ? product.getName() : "Product ID: " + rec.getProductId());
            System.out.printf("   Category: %s | Price: $%.2f\n",
                    product != null ? product.getCategory() : "Unknown",
                    product != null ? product.getPrice() : 0.0);
            System.out.printf("   🔥 Confidence Score: %.2f/5.0\n", normalizedScore);
            
            String stars = "";
            String emptyStars = "";
            int fullStars = Math.max(0, (int) Math.round(normalizedScore));
            int emptyCount = Math.max(0, 5 - fullStars);
            
            for (int i = 0; i < fullStars; i++) stars += "⭐";
            for (int i = 0; i < emptyCount; i++) emptyStars += "☆";
            
            System.out.println("   " + stars + emptyStars);
            System.out.println();
        }
    }
    
    public void runInteractiveSystem() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n🤖 AI-BASED RECOMMENDATION SYSTEM");
        System.out.println("==========================================");
        System.out.println("Welcome to the intelligent product recommendation system!");
        System.out.println("Using collaborative filtering algorithms.");
        
        while (true) {
            System.out.println("\n📋 MENU OPTIONS:");
            System.out.println("1. Get recommendations for a user");
            System.out.println("2. View user's existing ratings");
            System.out.println("3. Show product catalog");
            System.out.println("4. Show system statistics");
            System.out.println("5. Exit");
            System.out.print("\n👉 Choose an option (1-5): ");
            
            try {
                int choice = scanner.nextInt();
                
                switch (choice) {
                    case 1:
                        System.out.print("Enter User ID (1-50): ");
                        int userId = scanner.nextInt();
                        if (userId < 1 || userId > 50) {
                            System.out.println("❌ Invalid User ID. Please enter a number between 1-50.");
                            break;
                        }
                        
                        System.out.print("Number of recommendations (1-10): ");
                        int numRecs = scanner.nextInt();
                        if (numRecs < 1 || numRecs > 10) {
                            System.out.println("❌ Invalid number. Please enter 1-10.");
                            break;
                        }
                        
                        showUserRatings(userId);
                        displayRecommendations(userId, numRecs);
                        break;
                        
                    case 2:
                        System.out.print("Enter User ID (1-50): ");
                        userId = scanner.nextInt();
                        if (userId < 1 || userId > 50) {
                            System.out.println("❌ Invalid User ID. Please enter a number between 1-50.");
                            break;
                        }
                        showUserRatings(userId);
                        break;
                        
                    case 3:
                        productCatalog.displayCatalog();
                        break;
                        
                    case 4:
                        showSystemStatistics();
                        break;
                        
                    case 5:
                        System.out.println("👋 Thank you for using the AI Recommendation System!");
                        scanner.close();
                        return;
                        
                    default:
                        System.out.println("❌ Invalid option. Please choose 1-5.");
                }
                
            } catch (Exception e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
                scanner.nextLine();
            }
        }
    }
    
    private void showSystemStatistics() {
        System.out.println("\n📈 SYSTEM STATISTICS:");
        System.out.println("==========================================");
        System.out.printf("👥 Total Users: %d\n", userRatings.size());
        System.out.printf("📦 Total Products: %d\n", productCatalog.getAllProducts().size());
        
        int totalRatings = userRatings.values().stream()
                .mapToInt(Map::size)
                .sum();
        
        System.out.printf("⭐ Total Ratings: %d\n", totalRatings);
        System.out.printf("🧠 Algorithm: User-Based Collaborative Filtering\n");
        System.out.printf("📊 Similarity Metric: Pearson Correlation\n");
        System.out.printf("🎯 Similarity Threshold: 0.1\n");
        System.out.printf("📊 Average Ratings per User: %.2f\n",
                (double) totalRatings / userRatings.size());
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting AI-Based Recommendation System...");
        System.out.println("Built with standard Java libraries!");
        System.out.println("==========================================");
        
        SingleFileRecommendationSystem system = new SingleFileRecommendationSystem();
        system.runInteractiveSystem();
    }
    
    // Inner Classes
    static class Recommendation {
        private int productId;
        private double score;
        
        public Recommendation(int productId, double score) {
            this.productId = productId;
            this.score = score;
        }
        
        public int getProductId() { return productId; }
        public double getScore() { return score; }
    }
    
    static class Product {
        private int id;
        private String name;
        private String category;
        private double price;
        
        public Product(int id, String name, String category, double price) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        
        @Override
        public String toString() {
            return String.format("ID: %d | %s | %s | $%.2f", id, name, category, price);
        }
    }
    
    static class ProductCatalog {
        private Map<Integer, Product> products;
        
        public ProductCatalog() {
            this.products = new HashMap<>();
        }
        
        public void loadProducts(String filename) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                boolean isHeader = true;
                
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }
                    
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        try {
                            int id = Integer.parseInt(parts[0].trim());
                            String name = parts[1].trim();
                            String category = parts[2].trim();
                            double price = Double.parseDouble(parts[3].trim());
                            
                            products.put(id, new Product(id, name, category, price));
                        } catch (NumberFormatException e) {
                            System.err.println("⚠️ Skipping invalid product line: " + line);
                        }
                    }
                }
            }
            System.out.println("✅ Loaded " + products.size() + " products into catalog");
        }
        
        public Product getProduct(int id) {
            return products.get(id);
        }
        
        public Map<Integer, Product> getAllProducts() {
            return new HashMap<>(products);
        }
        
        public void displayCatalog() {
            System.out.println("\n🏪 PRODUCT CATALOG:");
            System.out.println("==========================================");
            
            Map<String, List<Product>> categoryMap = new HashMap<>();
            
            for (Product product : products.values()) {
                categoryMap.computeIfAbsent(product.getCategory(), category -> new ArrayList<>())
                        .add(product);
            }
            
            for (String category : categoryMap.keySet()) {
                System.out.println("\n📂 " + category.toUpperCase() + ":");
                System.out.println("------------------------------------------");
                List<Product> categoryProducts = categoryMap.get(category);
                categoryProducts.sort(Comparator.comparingInt(Product::getId));
                
                for (Product product : categoryProducts) {
                    System.out.println("  " + product);
                }
            }
            
            System.out.println("\nTotal Products: " + products.size());
        }
    }
}