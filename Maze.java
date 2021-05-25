package com.company;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.*;

public class Maze extends JPanel {

    final int nCols; // Number of columns
    final int nRows; // Number of rows
    final int cellSize = 25;
    final int margin = 25;

    final boolean[][] east_wall; // is easter wall of the cell full
    final boolean[][] north_wall; // is northern wall of the cell full

    int playerPositionX = 0;
    int playerPositionY = 0;


    /**
     * Initializes generating maze and listens to keyboard input
     * @param nRows - number of rows
     * @param nCols - number of columns
     * @param f - JFrame with maze
     */
    public Maze(int nRows, int nCols, JFrame f) {
        setPreferredSize(new Dimension(25*nCols+50, 25*nRows+50));
        setBackground(Color.white);
        this.nCols = nCols;
        this.nRows = nRows;
        east_wall = new boolean[nRows][nCols];
        north_wall = new boolean[nRows][nCols];
        generateMaze(nRows, nCols);


        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyTyped(e);

                int keyCode = e.getKeyCode();

                if(keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP)
                {
                    if(playerPositionY - 1 >= 0 && north_wall[playerPositionY][playerPositionX])
                        playerPositionY--;

                }else if(keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN)
                {
                    if(playerPositionY + 1 < nRows && north_wall[playerPositionY+1][playerPositionX])
                        playerPositionY++;

                }else if(keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT)
                {
                    if(playerPositionX - 1 >= 0 && east_wall[playerPositionY][playerPositionX-1])
                        playerPositionX--;

                }else if(keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT)
                {
                    if(playerPositionX + 1 < nCols && east_wall[playerPositionY][playerPositionX])
                        playerPositionX++;

                }

                animate();

                if(playerPositionX == nCols-1 && playerPositionY == nRows-1)
                {
                    victory();
                    f.dispose();
                }

            }
        });

    }


    /**
     * Needed for KeyListener
     * @return
     */
    public boolean isFocusTraversable ( ) {
        return true ;
    }


    /**
     * Draws maze and player moves
     * @param gg
     */
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.setStroke(new BasicStroke(5));
        g.setColor(Color.black);

        // draw maze
        for (int r = 0; r < nRows; r++) {
            for (int c = 0; c < nCols; c++) {

                int x = margin + c * cellSize;
                int y = margin + r * cellSize;

                if (!north_wall[r][c]) // N
                    g.drawLine(x, y, x + cellSize, y);

                if (r == nRows-1) // S - only for bottom cells
                    g.drawLine(x, y + cellSize, x + cellSize, y + cellSize);

                if (!east_wall[r][c]) // E
                    g.drawLine(x + cellSize, y, x + cellSize, y + cellSize);

                if (c == 0) // W - only for left most cells
                    g.drawLine(x, y, x, y + cellSize);

            }
        }

        // draws players moves
        int offset = margin + cellSize / 2;

        g.setColor(Color.blue);
        g.fillOval(offset - 5 + (playerPositionX*25), offset - 5 + (playerPositionY*25), 10, 10);

        // draws destination point
        g.setColor(Color.green);
        int x = offset + (nCols - 1) * cellSize;
        int y = offset + (nRows - 1) * cellSize;
        g.fillOval(x - 5, y - 5, 10, 10);

    }


    /**
     * Generates maze using way described in "KPG1_ENG.pdf"
     *
     * @param r - number of rows
     * @param c - number of columns
     */
    void generateMaze(int r, int c) {

        int x=0, y=0; // position of cell which is currently checked
        boolean[] withWall = new boolean[4]; // information which neighbours are unvisited
        boolean[][] visitedCell = new boolean[r][c]; // has this cell been visited
        int randomNumber; // random number for decision to which neighbour go next
        int howManyWalls; // checks if current cell has any unvisited neighbour
        LinkedList<Pair> location = new LinkedList<>(); // list of unvisited cells
        Pair pair; // position of unvisited cell and from which cell it was discovered

        // generates maze until every cell is visited
        do {

            // find a cell with unvisited neighbour
            do{

                howManyWalls = 0;

                if(x < r && y < c)
                {
                    if(y-1 >= 0 && !east_wall[x][y-1] && !visitedCell[x][y-1]) // left
                    {
                        withWall[0] = true;
                        howManyWalls++;
                    }

                    if(y+1 < c && !east_wall[x][y] && !visitedCell[x][y+1]) // right
                    {
                        withWall[1] = true;
                        howManyWalls++;
                    }

                    if(x-1 >= 0 && !north_wall[x][y] && !visitedCell[x-1][y]) // up
                    {
                        withWall[2] = true;
                        howManyWalls++;
                    }

                    if(x+1 < r && !north_wall[x+1][y] && !visitedCell[x+1][y]) // down
                    {
                        withWall[3] = true;
                        howManyWalls++;
                    }

                }

                // if all neighbours have been visited take position of a cell from list
                if(howManyWalls == 0)
                {

                    visitedCell[x][y] = true;

                    if(location.isEmpty())
                        break;

                    pair = location.pop();
                    x = pair.x;
                    y = pair.y;


                    if(!visitedCell[x][y])
                    {
                        if(pair.fromX > pair.x) // up
                        {
                            north_wall[pair.fromX][pair.fromY] = true;
                        }else if(pair.fromX < pair.x) // down
                        {
                            north_wall[pair.fromX+1][pair.fromY] = true;
                        }else if(pair.fromY > pair.y)
                        {
                            east_wall[pair.fromX][pair.fromY-1] = true;
                        }else if(pair.fromY < pair.y)
                        {
                            east_wall[pair.fromX][pair.fromY] = true;
                        }

                        visitedCell[x][y] = true;

                    }

                }

            }while(howManyWalls == 0 && !location.isEmpty());


            // randomly select neighbour
            do {
                randomNumber = ThreadLocalRandom.current().nextInt(0, 4);
            }while(!withWall[randomNumber] && (!location.isEmpty() || (x==0 && y==0)));


            // add rest of neighbours to list
            if(randomNumber != 0 && withWall[0])
                location.push(new Pair(x, y-1, x, y));

            if(randomNumber != 1 && withWall[1])
                location.push(new Pair(x, y+1, x, y));

            if(randomNumber != 2 && withWall[2])
                location.push(new Pair(x-1, y, x, y));

            if(randomNumber != 3 && withWall[3])
                location.push(new Pair(x+1, y, x, y));


            // "erase" the wall of newly visited cell
            if(!location.isEmpty() || (x==0 && y==0))
            {
                switch(randomNumber)
                {
                    case 0:
                        east_wall[x][y-1] = true;
                        y -= 1;
                        break;
                    case 1:
                        east_wall[x][y] = true;
                        y += 1;
                        break;
                    case 2:
                        north_wall[x][y] = true;
                        x -= 1;
                        break;
                    case 3:
                        north_wall[x+1][y] = true;
                        x += 1;
                        break;
                }
            }


            visitedCell[x][y] = true;

            Arrays.fill(withWall,Boolean.FALSE);

        } while(!location.isEmpty());


    }


    /**
     * Draw player movement
     */
    void animate() {
        try {
            Thread.sleep(20L);
        } catch (InterruptedException ignored) {
        }
        repaint();
    }

    /**
     * Main function
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Input");
            frame.setSize(320, 150);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel();
            frame.add(panel);
            placeComponents(panel, frame);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);


        });
    }


    /**
     * Displays window with generated maze
     * @param rows
     * @param cols
     */
    public static void maze(int rows, int cols)
    {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setTitle("Maze Generator");
        f.add(new Maze(rows,cols,f), BorderLayout.CENTER);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }


    /**
     * Displays window when player wins
     */
    public static void victory()
    {
        JFrame f = new JFrame();
        f.setSize(320, 110);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setTitle("You won!");

        JPanel panel = new JPanel();
        panel.setLayout(null);
        f.add(panel);

        JLabel rowsLabel = new JLabel("Congratulations!");
        rowsLabel.setBounds(100, 10, 110, 25);
        panel.add(rowsLabel);

        JButton close = new JButton("Close");
        close.setBounds(10, 40, 100, 25);
        panel.add(close);

        JButton oneMore = new JButton("Play one more");
        oneMore.setBounds(170, 40, 120, 25);
        panel.add(oneMore);

        f.setLocationRelativeTo(null);
        f.setVisible(true);

        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

        oneMore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                String[] args = new String[0];
                main(args);
                f.dispose();
            }
        });

    }


    /**
     * Asks player about the size of a maze
     * @param panel
     * @param frame
     */
    private static void placeComponents(JPanel panel, JFrame frame) {

        panel.setLayout(null);

        JLabel rowsLabel = new JLabel("Number of rows: ");
        rowsLabel.setBounds(10, 10, 110, 25);
        panel.add(rowsLabel);

        JTextField rows = new JTextField(20);
        rows.setBounds(130, 10, 160, 25);
        panel.add(rows);

        JLabel columnsLabel = new JLabel("Number of columns: ");
        columnsLabel.setBounds(10, 40, 120, 25);
        panel.add(columnsLabel);

        JTextField columns = new JTextField(20);
        columns.setBounds(130, 40, 160, 25);
        panel.add(columns);

        JButton generate = new JButton("Generate");
        generate.setBounds(180, 80, 100, 25);
        panel.add(generate);

        generate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                frame.dispose();
                int nR;
                int nC;
                try{
                    nR = Integer.parseInt(rows.getText());
                    if(nR<2)
                        nR=2;
                }catch (Exception ex)
                {
                    nR = 10;
                }

                try{
                    nC = Integer.parseInt(columns.getText());
                    if(nC<2)
                        nC=2;
                }catch (Exception ex)
                {
                    nC = 10;
                }

                maze(nR,nC);
            }
        });

    }


    /**
     * Contains information about cell in the list
     */
    public static class Pair
    {
        int x; // x coordinate of a cell which can be visited
        int y;
        int fromX; // x coordinate of a cell from which the new cell was discovered
        int fromY;

        public Pair(int x, int y, int fromX, int fromY)
        {
            this.x = x;
            this.y = y;
            this.fromX = fromX;
            this.fromY = fromY;
        }

    }

}