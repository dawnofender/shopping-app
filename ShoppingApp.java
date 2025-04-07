import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
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

public class ShoppingApp {

    public static void main(String[] args) {
        int windowWidth = 1024;
        int windowHeight = 1024;

        ShopUI ui = new ShopUI(windowWidth, windowHeight, "villager trades");
        ui.setVisible(true);

    }
    
}
