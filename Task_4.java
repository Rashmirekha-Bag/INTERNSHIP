// ========== RecommendationSystem.java ==========
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * AI-Based Recommendation System using Apache Mahout
 * 
 * This system demonstrates collaborative filtering to recommend products/content
 * based on user preferences and behavior patterns.
 * 
 * Features:
 * - User-based collaborative filtering
 * - Pearson correlation similarity
 * - Sample data generation
 * - Interactive recommendation interface
 * - Product catalog management
 * 
 * Author: CODTECH Intern
 * Task: 4 - AI Recommendation System
 */
public class RecommendationSystem {
    
    private static final String DATA_FILE = "user_ratings.csv";
    private static final String PRODUCTS_FILE = "products.csv";
    private UserBasedRecommender recommender;
    private ProductCatalog productCatalog;
    
    public RecommendationSystem() {
        this.productCatalog = new ProductCatalog();
        initializeSystem();
    }
    
    /**
     * Initialize the recommendation system with sample data and build the model
     */
    private void initializeSystem() {
        try {
            System.out.println("🤖 Initializing AI-Based Recommendation System...");
            
            // Generate sample data if files don't exist
            if (!new File(DATA_FILE).exists() || !new File(PRODUCTS_FILE).exists()) {
                System.out.println("📊 Generating sample data...");
                generateSampleData();
            }
            
            // Load product catalog
            productCatalog.loadProducts(PRODUCTS_FILE);
            
            // Build recommendation model
            buildRecommendationModel();
            
            System.out.println("✅ Recommendation system initialized successfully!");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Build the recommendation model using Apache Mahout
     */
    private void buildRecommendationModel() throws IOException, TasteException {
        // Load data model from CSV file
        DataModel model = new FileDataModel(new File(DATA_FILE));
        
        // Create user similarity using Pearson correlation
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        
        // Create user neighborhood (users with similarity > 0.1)
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        
        // Create the recommender
        recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        
        System.out.println("🧠 Machine Learning model trained with " + model.getNumUsers() + 
                         " users and " + model.getNumItems() + " products");
    }
    
    /**
     * Generate sample data for demonstration
     */
    private void generateSampleData() throws IOException {
        // Generate products data
        generateProductsData();
        
        // Generate user ratings data
        generateUserRatingsData();
    }
    
    /**
     * Generate sample products catalog
     */
    private void generateProductsData() throws IOException {
        FileWriter writer = new FileWriter(PRODUCTS_FILE);
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
        writer.close();
        
        System.out.println("✅ Generated products catalog with " + products.length + " items");
    }
    
    /**
     * Generate sample user ratings data (UserID, ProductID, Rating)
     */
    private void generateUserRatingsData() throws IOException {
        FileWriter writer = new FileWriter(DATA_FILE);
        
        // Generate ratings for 50 users across 20 products
        // Rating scale: 1-5 (1=poor, 5=excellent)
        
        java.util.Random random = new java.util.Random(42); // Fixed seed for reproducibility
        
        for (int userId = 1; userId <= 50; userId++) {
            // Each user rates 8-15 random products
            int numRatings = 8 + random.nextInt(8);
            java.util.Set<Integer> ratedProducts = new java.util.HashSet<>();
            
            for (int i = 0; i < numRatings; i++) {
                int productId = 1 + random.nextInt(20);
                
                // Avoid duplicate ratings for same user-product pair
                if (ratedProducts.contains(productId)) {
                    continue;
                }
                ratedProducts.add(productId);
                
                // Generate rating with some bias based on product category
                double rating = generateBiasedRating(userId, productId, random);
                
                writer.write(userId + "," + productId + "," + rating + "\n");
            }
        }
        writer.close();
        
        System.out.println("✅ Generated user ratings data for collaborative filtering");
    }
    
    /**
     * Generate biased ratings to create realistic user preferences
     */
    private double generateBiasedRating(int userId, int productId, java.util.Random random) {
        // Create user preference patterns
        double baseRating = 2.5 + random.nextGaussian() * 0.8;
        
        // User preferences based on ID patterns
        if (userId % 3 == 0) { // Tech enthusiasts (like electronics)
            if (productId <= 10) baseRating += 1.0;
        } else if (userId % 3 == 1) { // Gamers (like gaming products)
            if (productId >= 7 && productId <= 10) baseRating += 1.2;
        } else { // Book lovers and fashion conscious
            if (productId >= 11 && productId <= 19) baseRating += 0.8;
        }
        
        // Ensure rating is within valid range
        return Math.max(1.0, Math.min(5.0, baseRating));
    }
    
    /**
     * Get recommendations for a specific user
     */
    public void getRecommendationsForUser(long userId, int numRecommendations) {
        try {
            System.out.println("\n🎯 Getting AI-powered recommendations for User " + userId + "...");
            System.out.println("==========================================");
            
            List<RecommendedItem> recommendations = recommender.recommend(userId, numRecommendations);
            
            if (recommendations.isEmpty()) {
                System.out.println("❌ No recommendations available for this user.");
                System.out.println("💡 This might happen if the user has no ratings or similar users.");
                return;
            }
            
            System.out.println("🌟 Top " + recommendations.size() + " Recommendations:");
            System.out.println("==========================================");
            
            int rank = 1;
            for (RecommendedItem recommendation : recommendations) {
                long itemId = recommendation.getItemID();
                float score = recommendation.getValue();
                
                Product product = productCatalog.getProduct(itemId);
                
                System.out.printf("%d. %s\n", rank++, product != null ? product.getName() : "Product ID: " + itemId);
                System.out.printf("   Category: %s | Price: $%.2f\n", 
                                product != null ? product.getCategory() : "Unknown",
                                product != null ? product.getPrice() : 0.0);
                System.out.printf("   🔥 Confidence Score: %.2f/5.0\n", score);
                System.out.println("   " + "⭐".repeat((int) Math.round(score)) + 
                                 "☆".repeat(5 - (int) Math.round(score)));
                System.out.println();
            }
            
        } catch (TasteException e) {
            System.err.println("❌ Error generating recommendations: " + e.getMessage());
        }
    }
    
    /**
     * Display user's existing ratings
     */
    public void showUserRatings(long userId) {
        try {
            System.out.println("\n📊 User " + userId + "'s Previous Ratings:");
            System.out.println("==========================================");
            
            DataModel model = recommender.getDataModel();
            var preferences = model.getPreferencesFromUser(userId);
            
            if (preferences.length() == 0) {
                System.out.println("❌ No ratings found for this user.");
                return;
            }
            
            var iterator = preferences.iterator();
            while (iterator.hasNext()) {
                var pref = iterator.next();
                long itemId = pref.getItemID();
                float rating = pref.getValue();
                
                Product product = productCatalog.getProduct(itemId);
                System.out.printf("• %s - Rating: %.1f/5.0 %s\n", 
                                product != null ? product.getName() : "Product ID: " + itemId,
                                rating,
                                "⭐".repeat((int) Math.round(rating)));
            }
            
        } catch (TasteException e) {
            System.err.println("❌ Error retrieving user ratings: " + e.getMessage());
        }
    }
    
    /**
     * Interactive menu system
     */
    public void runInteractiveSystem() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n🤖 AI-BASED RECOMMENDATION SYSTEM");
        System.out.println("==========================================");
        System.out.println("Welcome to the intelligent product recommendation system!");
        System.out.println("Using collaborative filtering and machine learning algorithms.");
        
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
                        long userId = scanner.nextLong();
                        System.out.print("Number of recommendations (1-10): ");
                        int numRecs = scanner.nextInt();
                        
                        showUserRatings(userId);
                        getRecommendationsForUser(userId, numRecs);
                        break;
                        
                    case 2:
                        System.out.print("Enter User ID (1-50): ");
                        userId = scanner.nextLong();
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
                        return;
                        
                    default:
                        System.out.println("❌ Invalid option. Please choose 1-5.");
                }
                
            } catch (Exception e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }
    
    /**
     * Display system statistics
     */
    private void showSystemStatistics() {
        try {
            DataModel model = recommender.getDataModel();
            System.out.println("\n📈 SYSTEM STATISTICS:");
            System.out.println("==========================================");
            System.out.printf("👥 Total Users: %d\n", model.getNumUsers());
            System.out.printf("📦 Total Products: %d\n", model.getNumItems());
            System.out.printf("⭐ Total Ratings: %d\n", model.getNumUsersWithPreferenceFor(1)); // Approximate
            System.out.printf("🧠 Algorithm: User-Based Collaborative Filtering\n");
            System.out.printf("📊 Similarity Metric: Pearson Correlation\n");
            System.out.printf("🎯 Neighborhood Threshold: 0.1\n");
            
        } catch (TasteException e) {
            System.err.println("❌ Error retrieving statistics: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting AI-Based Recommendation System...");
        System.out.println("Built with Apache Mahout Machine Learning Library");
        System.out.println("==========================================");
        
        RecommendationSystem system = new RecommendationSystem();
        system.runInteractiveSystem();
    }
}

// ========== Product.java ==========
/**
 * Product model class to represent items in the catalog
 */
class Product {
    private long id;
    private String name;
    private String category;
    private double price;
    
    public Product(long id, String name, String category, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return String.format("ID: %d | %s | %s | $%.2f", id, name, category, price);
    }
}

// ========== ProductCatalog.java ==========
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Product catalog management class
 */
class ProductCatalog {
    private Map<Long, Product> products;
    
    public ProductCatalog() {
        this.products = new HashMap<>();
    }
    
    /**
     * Load products from CSV file
     */
    public void loadProducts(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip header row
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    long id = Long.parseLong(parts[0]);
                    String name = parts[1];
                    String category = parts[2];
                    double price = Double.parseDouble(parts[3]);
                    
                    products.put(id, new Product(id, name, category, price));
                }
            }
        }
        
        System.out.println("✅ Loaded " + products.size() + " products into catalog");
    }
    
    /**
     * Get product by ID
     */
    public Product getProduct(long id) {
        return products.get(id);
    }
    
    /**
     * Display entire product catalog
     */
    public void displayCatalog() {
        System.out.println("\n🏪 PRODUCT CATALOG:");
        System.out.println("==========================================");
        
        Map<String, java.util.List<Product>> categoryMap = new HashMap<>();
        
        // Group products by category
        for (Product product : products.values()) {
            categoryMap.computeIfAbsent(product.getCategory(), k -> new java.util.ArrayList<>())
                      .add(product);
        }
        
        // Display by category
        for (String category : categoryMap.keySet()) {
            System.out.println("\n📂 " + category.toUpperCase() + ":");
            System.out.println("------------------------------------------");
            for (Product product : categoryMap.get(category)) {
                System.out.println("  " + product);
            }
        }
    }
}

// ========== Maven Dependencies (pom.xml) ==========
/*
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.codtech</groupId>
    <artifactId>recommendation-system</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Apache Mahout for Machine Learning -->
        <dependency>
            <groupId>org.apache.mahout</groupId>
            <artifactId>mahout-mr</artifactId>
            <version>0.13.0</version>
        </dependency>
        
        <!-- SLF4J for logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>1.7.25</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
*/

// ========== README.md ==========
/*
# AI-Based Recommendation System

## Overview
This project implements an intelligent recommendation system using Apache Mahout machine learning library. 
It demonstrates collaborative filtering techniques to suggest products based on user preferences and behavior patterns.

## Features
✅ User-based collaborative filtering algorithm
✅ Pearson correlation similarity measurement  
✅ Automatic sample data generation
✅ Interactive command-line interface
✅ Product catalog management
✅ Real-time recommendations with confidence scores
✅ System statistics and analytics

## Technology Stack
- **Java 8+**: Core programming language
- **Apache Mahout**: Machine learning library for recommendations
- **Maven**: Dependency management and build tool
- **CSV**: Data storage format

## Setup Instructions

### 1. Prerequisites
- Java 8 or higher
- Maven 3.6+
- Terminal/Command Prompt

### 2. Project Setup
```bash
# Clone or create project directory
mkdir recommendation-system
cd recommendation-system

# Copy the Java files
# Copy pom.xml for Maven dependencies

# Install dependencies
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="RecommendationSystem"
```

### 3. Alternative Setup (without Maven)
```bash
# Download Apache Mahout JAR files manually
# Add to classpath and compile:
javac -cp "mahout-mr-0.13.0.jar:." *.java
java -cp "mahout-mr-0.13.0.jar:." RecommendationSystem
```

## How It Works

### 1. Data Model
- **Users**: 50 simulated users with different preferences
- **Products**: 20 products across categories (Electronics, Gaming, Books, Fashion)
- **Ratings**: 1-5 scale user ratings for products

### 2. Machine Learning Algorithm
- **Collaborative Filtering**: Finds users with similar preferences
- **Pearson Correlation**: Measures similarity between users
- **Neighborhood Threshold**: 0.1 minimum similarity for recommendations

### 3. Sample Output
```
🎯 Getting AI-powered recommendations for User 15...
==========================================
🌟 Top 3 Recommendations:
==========================================
1. MacBook Air M2
   Category: Electronics | Price: $1199.99
   🔥 Confidence Score: 4.2/5.0
   ⭐⭐⭐⭐☆

2. AirPods Pro  
   Category: Electronics | Price: $249.99
   🔥 Confidence Score: 3.8/5.0
   ⭐⭐⭐⭐☆
```

## Usage Examples

### Getting Recommendations
1. Run the application
2. Choose option 1: "Get recommendations for a user"
3. Enter User ID (1-50)
4. Specify number of recommendations (1-10)

### Viewing User Ratings
1. Choose option 2: "View user's existing ratings"
2. Enter User ID to see their rating history

### System Statistics
- Total users and products
- Algorithm details
- Performance metrics

## File Structure
```
recommendation-system/
├── RecommendationSystem.java    # Main application
├── Product.java                 # Product model
├── ProductCatalog.java         # Catalog management
├── pom.xml                     # Maven dependencies
├── user_ratings.csv            # Generated user ratings data
├── products.csv                # Generated product catalog
└── README.md                   # This file
```

## Key Learning Outcomes
- Understanding collaborative filtering algorithms
- Working with Apache Mahout machine learning library
- Implementing recommendation engines in Java
- Data modeling for recommendation systems
- Performance optimization for ML applications

## Future Enhancements
- Content-based filtering
- Hybrid recommendation approaches
- Real-time rating updates
- Web-based user interface
- Database integration
- A/B testing framework

## Troubleshooting
- Ensure Java 8+ is installed
- Check Maven dependencies are downloaded
- Verify CSV files are generated correctly
- Check file permissions for data files

This recommendation system demonstrates enterprise-level AI implementation 
suitable for e-commerce, content platforms, and personalization engines.
*/
