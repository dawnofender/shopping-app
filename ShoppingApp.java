import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.io.File;
import java.io.FileWriter;   // Import the FileWriter class
import java.util.Scanner; // Import the Scanner class to read text files
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;


class oldbuttonAction implements ActionListener {
    
    JLabel label;
    JTextField textBox;
    
    public oldbuttonAction(JLabel label, JTextField textField) {
        this.label = label;
        this.textBox = textField;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String inputText = textBox.getText();
        label.setText(inputText);
    }
}


class ImagePanel extends JPanel {
    
    private BufferedImage image;
    private int width;
    private int height;

    public ImagePanel(String imagePath) {
        try {
            File myFile = new File(imagePath);  
            image = ImageIO.read(myFile); 
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public void setWidth(int w) {
        this.width = w;
    }
    
    public void setHeight(int h) {
        this.height = h;
    }

    @Override
    protected void paintComponent(Graphics g) {
        
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, width, height, null);
        }
    }
}

class ShopItem {
    private String name;
    private String description;
    private ImagePanel image;
    
    public ShopItem(String n, String d, String i) { 
        this.name = n;
        this.description = d;
        this.image = new ImagePanel(i);
    }

    public ShopItem(ShopItem source) {
        this.name = source.name;
        this.description = source.description;
        this.image = source.image;
    }
    
    public String getName() {
        return(this.name);
    }

    public String getDescription() {
        return(this.description);
    }

    public ImagePanel getImage() {
        return(this.image);
    }

    @Override
    public String toString() {
        return name + description;
    }

    
}


class Inventory {
    private List<ShopItem> items = new ArrayList<ShopItem>();
    private String name;
    
    public Inventory(String n){
        this.name = n;
    }
    
    private void sortItems() {
        // insertion sort
        for(int i=0; i<items.size(); i++) {
            for (int j=i; j>0; j--) {
                String itemA = items.get(j).toString();
                String itemB = items.get(j-1).toString();
                int compare = itemA.compareTo(itemB);
                if(compare > 0) {
                    ShopItem temp = new ShopItem(items.get(j));
                    items.set(j, items.get(j-1));
                    items.set(j-1, temp);
                }
            }
        }
    }

    public String getName() {
        return(name);
    }

    public void addItem(ShopItem item) {
        items.add(item);
    }

    public void removeItem(ShopItem item) {
        items.remove(item);
    }

    public void loadItems() {
        // read items from inventory.txt
        try {
            File file = new File("inventory.txt");
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] parts = data.split(":");
                addItem(new ShopItem(parts[0], parts[1], parts[2]));
                System.out.println(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while loading data from inventory.txt.");
            e.printStackTrace();
        }
    }


    public void saveItems() {
        // write items to inventory.txt

        try {
            File file = new File("inventory.txt");
            FileWriter writer = new FileWriter("inventory.txt");
            for (ShopItem item : items) {
                writer.write(item.getName() + ":" + item.getDescription() + ":" + item.getImage());
                writer.write("\n");
                System.out.println(item);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while writing items to inventory.txt.");
            e.printStackTrace();
        }
    }
    
    private int binarySearch(String term, int start, int end) {
        int searchInd = (start + end) / 2;
        int compare = items.get(searchInd).toString().compareTo(term);
       
        if(compare > 0 && searchInd-1 >= 0) {
            return binarySearch(term, start, searchInd-1);
        } else if (compare < 0 && searchInd+1 < items.size()) {
            return binarySearch(term, searchInd+1, end);
        } else if (compare == 0){
            return searchInd;
        }
        //no results?
        return -1;
    }

    // private int similarity(String a, String b) {
    //     return(a.compare(b) - a.length() - b.length()); // cancel out differences in string lengths
    // }


    private float calculateSimilarity(ShopItem item, String term) {
        // break the term into pieces of decreasing size,
        // comparing it to the current item. 
        // Bigger pieces == more similarity
        float similarity = 0;
        float maxSimilarity = 0;
        
        for(int j=0; j<term.length(); j++) {
            for (int k=j; k<term.length(); ++k) {
                if(item.toString().contains(term.substring(j, k))) {
                    similarity += k-j;
                }                    
                maxSimilarity += k-j;
            }
        }

        return(similarity / maxSimilarity);
    }

    public List<ShopItem> search(String term, int minP) {
        sortItems();
        List<ShopItem> results = new ArrayList<ShopItem>(items);
        float[] scoreboard = new float[results.size()]; // score is similarity from 0 to 1

        if (results.size() < 1) {
            results.add(new ShopItem("no items :(", "", "assets/Golden_Carrot.png"));
            return(results);
        }
        System.out.println(term);

        // start with fast binary search. This will only include a perfect match, but it's instant
        int result = binarySearch(term, 0, results.size());
        if(result != -1) {
            // swap result to first item
            ShopItem temp = results.get(0);
            results.set(0, results.get(result));
            results.set(result, temp);
            scoreboard[0] = 1;
        } else {
            scoreboard[0] = calculateSimilarity(results.get(0), term);
        }

        // now insertion sort all items by similarity to the search term
        for(int i = 1; i < results.size(); i++) {
            scoreboard[i] = calculateSimilarity(results.get(i), term);
            
            float key = scoreboard[i];
            ShopItem temp = results.get(i);

            int j = i-1;
            while(j >= 0 && key > scoreboard[j]) {
                results.set(j+1, results.get(j));
                scoreboard[j+1] = scoreboard[j];
                j--;
            }
            results.set(j+1, temp);
            scoreboard[j+1] = key;
        }

        return(results);
    }

    public List<ShopItem> search(String term) {
        int minP = term.length()/3;
        return(search(term, minP));
    }

    public ShopItem getItem(int i) {
        return(items.get(i));
    }

    public ShopItem getItem(String term) {
        ShopItem item = search(term).get(0);
        return(item);
    }
}


class ActionSearch implements ActionListener {
    
    private SearchUI searchUI;
    
    public ActionSearch(SearchUI s) {
        this.searchUI = s;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        searchUI.updateResults();
    }
}


class ActionAddItem implements ActionListener {
    
    private SearchUI searchUI;
    
    public ActionAddItem(SearchUI s) {
        this.searchUI = s;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        searchUI.sendToInventory();
        searchUI.updateResults();
    }
}

class ActionRemoveItem implements ActionListener {
    private Inventory inventory;
    private ShopItem item;
    private SearchUI searchUI;

    public ActionRemoveItem(SearchUI ui, Inventory inventory, ShopItem item) {
        this.searchUI = ui;
        this.inventory = inventory;
        this.item = item;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        inventory.removeItem(item);
        searchUI.updateResults();
    }
}

class ActionSave implements ActionListener {
    private Inventory inventory;

    public ActionSave(Inventory inventory) {
        this.inventory = inventory;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        inventory.saveItems();
    }
}

class ActionLoad implements ActionListener {
    private Inventory inventory;

    public ActionLoad(Inventory inventory) {
        this.inventory = inventory;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        inventory.loadItems();
    }
}

public class SearchUI extends JFrame {
    private JTextField searchField = new JTextField(20);
    private JTextField nameField = new JTextField(20);
    private JTextField descriptionField = new JTextField(20);
    private JTextField imagePathField = new JTextField(20);
    private JButton searchButton = new JButton("Search");  
    private JPanel resultsPanel = new JPanel();
    private Inventory inventory;

    public SearchUI(int width, int height, Inventory i) {
        this.setSize(width, height);
        this.inventory = i;
        setup();
    }

    private void setup() {
        Color bgColour = new Color(191, 191, 191); 
        this.getContentPane().setBackground(bgColour);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null); 

        //search area
        JLabel inventoryTitle = new JLabel(inventory.getName());
        inventoryTitle.setBounds(64, 128-32, 256, 32);
        this.add(inventoryTitle);

        searchField.setBounds(64, 128, 256, 32);
        this.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBounds(64, 128+32, 256, 32);
        this.add(searchButton);
        searchButton.addActionListener(new ActionSearch(this));

        // Inventory management (left side)
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        saveButton.setBounds(384, 64, 96, 32);
        loadButton.setBounds(544, 64, 96, 32);
        this.add(saveButton);
        this.add(loadButton);
        saveButton.addActionListener(new ActionSave(inventory));
        loadButton.addActionListener(new ActionLoad(inventory));
        
        JLabel info = new JLabel("name, description, image path");
        info.setBounds(384, 128-32, 256, 32);
        this.add(info);
        
        nameField.setBounds(        384, 128,    256, 32);
        descriptionField.setBounds( 384, 128+32, 256, 32);
        imagePathField.setBounds(   384, 128+64, 256, 32);
        this.add(nameField);
        this.add(descriptionField);
        this.add(imagePathField);

        JButton addButton = new JButton("Add");
        addButton.setBounds(384, 128+96, 256, 32);
        this.add(addButton);
        addButton.addActionListener(new ActionAddItem(this));
        this.add(searchField);
        
        resultsPanel.setBounds(64, 256, 256, 256);
        resultsPanel.setLayout(null);
        this.add(resultsPanel);

        this.setVisible(true);
    }

    public void sendToInventory() {
        String name = nameField.getText();
        String desc = descriptionField.getText();
        String path = imagePathField.getText();
        inventory.addItem(new ShopItem(name, desc, path));
    }


    public void updateResults() {
        String query = searchField.getText();
        if(query.length() < 1) {
            return;
        }
        List<ShopItem> results = inventory.search(query);

        int itemHeight = 64;
        int itemWidth = 256;
        int imageSize = 64;
        int textWidth = 160;
        int textHeight = 16;
        int buttonWidth = 92;

        int margins = 8;
        int itemOffset = 0;

        // clear previous search results
        resultsPanel.removeAll();

        for(int i = 0; i < results.size(); i++) {
            ShopItem item = results.get(i);

            JPanel itemPanel = new JPanel();
            itemPanel.setBounds(0, itemOffset, itemWidth, itemHeight);
            itemPanel.setLayout(null);
            itemOffset += itemHeight;

            ImagePanel iImage = item.getImage();
            iImage.setWidth(imageSize);
            iImage.setHeight(imageSize);
            iImage.setBounds(
                0,
                0, 
                imageSize, 
                imageSize
            );
            itemPanel.add(iImage);
            
            JLabel title = new JLabel(item.getName());
            title.setBounds(
                imageSize + margins*2,
                margins, 
                textWidth, 
                textHeight
            );
            itemPanel.add(title);

            JLabel description = new JLabel(item.getDescription());
            description.setBounds(
                imageSize + margins*2,
                margins + textHeight, 
                textWidth, 
                textHeight
            );
            itemPanel.add(description);
            
            JButton removeButton = new JButton("Remove");
            removeButton.setBounds(
                imageSize + margins*2,
                textHeight*2 + margins*2,
                buttonWidth,
                textHeight
            );
            removeButton.addActionListener(new ActionRemoveItem(this, inventory, item));
            itemPanel.add(removeButton);

            resultsPanel.add(itemPanel);
        }
        this.repaint();
        this.revalidate();
    }
    
}



public class DisplayBufferedImage {
    public static void main(String[] args) {
    }
}


public class ShoppingApp {

    public static void main(String[] args) {
        Inventory villagerTrades = new Inventory("villager trades");
        
        ShopItem goldenCarrot = new ShopItem("Golden Carrot", "Restores 6 hunger and 14.4 saturation.", "assets/Golden_Carrot.png");
        ShopItem steak = new ShopItem("Steak", "Restores 6 hunger and 14.4 saturation.", "assets/Golden_Carrot.png");
        ShopItem rottenFlesh = new ShopItem("Rotten Flesh", "Restores 6 hunger and 14.4 saturation.", "assets/Golden_Carrot.png");
        villagerTrades.addItem(goldenCarrot);
        villagerTrades.addItem(steak);
        villagerTrades.addItem(rottenFlesh);
        
        int windowWidth = 1024;
        int windowHeight = 1024;

        SearchUI ui = new SearchUI(windowWidth, windowHeight, villagerTrades);
        ui.setVisible(true);

    }
    
}
