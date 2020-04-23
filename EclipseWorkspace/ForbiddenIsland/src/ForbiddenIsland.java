// Assignment 9
// Harmon Thomas
// tharmon
// Song Yangsoo
// songyang25


import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;


// Represents a single square of the game area
class Cell {
    // represents absolute height of this cell, in feet
    double height;
    // In logical coordinates, with the origin at the top-left corner of the screen
    int x;
    int y;
    // the four adjacent cells to this one
    Cell left;
    Cell top;
    Cell right;
    Cell bottom;
    // reports whether this cell is flooded or not
    boolean isFlooded;

    // create a new Cell 
    Cell(double height, int x, int y, 
            Cell left, Cell top, Cell right, Cell bottom, boolean isFlooded) {
        this.height = height;
        this.x = x;
        this.y = y;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.isFlooded = isFlooded;
    }

    // create a new cell without any neighbors
    Cell(double height, int x, int y, boolean isFlooded) {
        this.height = height;
        this.x = x;
        this.y = y;
        this.isFlooded = isFlooded;
    }

    // measure and create the color of this cell
    public Color cellColor(int waterHeight) {
        int islandHeight = ForbiddenIslandWorld.ISLAND_SIZE / 2;
        // the rgb values for the color and a scalar
        int r;
        int g;
        int b;
        // a scalar to change the rgb values
        double k;
        // the initial values for the colors of the cells
        int rInit = 18;
        int gInit = 182;
        int bInit = 12;

        if (!this.isFlooded && (this.height - waterHeight >= 0)) {
            k = (int)(this.height / islandHeight);
            r = (int)(Math.min(255, rInit + (255 / islandHeight) * this.height));
            g = (int)(Math.min(255, gInit + (255 / islandHeight) * this.height));
            b = (int)(Math.min(255, bInit + (255 / islandHeight) * this.height));
        }
        // not final implementation, to be tested and refined once 
        // flooding is implemented in part 2
        else if (!this.isFlooded && this.height - waterHeight < 0) {
            k = (int)(Math.abs(this.height - waterHeight));
            r = (int)(Math.min(255, rInit + k));
            g = (int)(Math.max(0, gInit - k));
            b = (int)(Math.max(0, bInit - k));
        }
        // not final implementation, to be tested and refined 
        // once flooding is implemented in part 2
        else {
            k = (int)Math.abs(this.height - waterHeight);
            r = 0;
            g = 0;
            b = (int)(Math.max(0, (bInit - k)));
        }
        return new Color(r, g, b);
    }


    // draw the given cell onto the given scene
    public void drawCell(Cell c, WorldScene s, int waterHeight) {
        s.placeImageXY(
                new RectangleImage(
                        ForbiddenIslandWorld.CELL_SIZE,
                        ForbiddenIslandWorld.CELL_SIZE,
                        OutlineMode.SOLID,
                        c.cellColor(waterHeight)),
                c.x * ForbiddenIslandWorld.CELL_SIZE + ForbiddenIslandWorld.CELL_SIZE / 2,
                c.y * ForbiddenIslandWorld.CELL_SIZE + ForbiddenIslandWorld.CELL_SIZE / 2);
    }

    // Check to flood additional cells on each on tick.
    // EFFECT: changes this.isFlooded
    public void floodCell(int waterHeight) {
        if ((!this.isFlooded)
                && this.isCoastline() 
                && this.height < waterHeight) {
            this.isFlooded = true;
        }
    }

    // is this cell a coastline cell
    boolean isCoastline() {
        return
                this.top.isFlooded || this.left.isFlooded ||
                this.bottom.isFlooded || this.right.isFlooded;
    }
}


class OceanCell extends Cell {
    // create a new OceanCell
    // the height will always be zero since OceanCells are at sea level
    // oceancells are always flooded
    OceanCell(int x, int y, Cell left, Cell top, Cell right, Cell bottom) {
        super(0, x, y, left, top, right, bottom, true);
    }

    // create a new OceanCell without any neighbours
    // the height will always be zero since OceanCells are at sea level
    // oceancells are always flooded
    OceanCell(int x, int y) {
        super(0, x, y, true);
    }

    @Override
    // draw this cell
    public void drawCell(Cell c, WorldScene s, int waterHeight) {
        s.placeImageXY(
                new RectangleImage(
                        ForbiddenIslandWorld.CELL_SIZE,
                        ForbiddenIslandWorld.CELL_SIZE,
                        OutlineMode.SOLID,
                        Color.BLUE),
                c.x * ForbiddenIslandWorld.CELL_SIZE + ForbiddenIslandWorld.CELL_SIZE / 2,
                c.y * ForbiddenIslandWorld.CELL_SIZE + ForbiddenIslandWorld.CELL_SIZE / 2);
    }

    // return false since this cell is never a coastline
    public boolean isCoastline() {
        return false;
    }
}



abstract class Piece {
    Cell loc;
}



// to represent a pilot
class Pilot extends Piece {

    // the image of the pilot
    WorldImage pilot;
    // the cell where the pilot is located
    Cell loc;
    // the number of ticks of scuba tank left
    int scuba;
    // represents if the scuba is on or not
    boolean scubaOperational;


    Pilot(Cell loc) {
        this.pilot = new FromFileImage("pilot.png");
        this.loc = loc;
        this.scuba = 0;
        this.scubaOperational = false;
    }

    // check is the player died
    boolean drowned() {
        if (this.loc.isFlooded) {
            return !(this.scubaOperational && this.scuba > 0);
        }
        else {
            return false;
        }
    }

    // turn the scuba on or off
    void toggleScuba() {
        if (this.scuba > 0) {
            this.scubaOperational = !this.scubaOperational;
        }
        else {
            this.scubaOperational = false;
        }
    }

    // initialize the scuba tank
    void initScuba() {
        this.scuba = 100;
    }

    // decrease the oxygen levels in the scuba tank
    void useScuba() {
        if (this.scubaOperational && (this.scuba > 0)) {
            this.scuba -= 1;
        }
        else {
            this.scubaOperational = false;
        }
    }
}


//to represent the targets
class Target extends Piece {

    WorldImage target;
    Cell loc;
    boolean isCollected;

    Target(Cell loc) {
        this.target = new CircleImage(5, OutlineMode.SOLID, Color.red);
        this.isCollected = false;
        this.loc = loc;
    }
}



//to represent the heicopter
class Heli extends Piece {

    WorldImage image;
    Cell loc;


    Heli(Cell loc) {
        this.image = new FromFileImage("helicopter.png");
        this.loc = loc;
    }
}

//to represent the scuba piece
class ScubaTarget extends Piece {
    WorldImage image = new FromFileImage("scuba.png");

    // create a new scuba piece
    ScubaTarget(Cell loc) {
        this.loc = loc;
    }

    // have the given player process this scuba piece
    // by giving them 100 ticks worth of oxygen
    void process(Pilot player, ScubaTarget scuba) {
        if (player.loc == scuba.loc) {
            player.initScuba();
        }
    }
}


class ForbiddenIslandWorld extends World {
    // defines a constant for the size of the island
    static final int ISLAND_SIZE = 64;
    // defines a constant for the size of a cell
    static final int CELL_SIZE = 10;
    // defines a constant for island height
    static final int ISLAND_HEIGHT = ISLAND_SIZE / 2;
    // a random generator
    Random rand = new Random();
    // All the cells of the game, including the ocean
    IList<Cell> board;
    // the current height of the ocean
    int waterHeight;
    // the timer for ontick
    int timer = 0;
    // the player's score
    int score = 0;
    // the number of pieces collected
    int targetsCollected = 0;

    // an Array list of Cells
    ArrayList<ArrayList<Cell>> cells;
    // All cells height represented in Arraylists
    ArrayList<ArrayList<Double>> cellHeights;

    // represents player1
    Pilot pilot;
    // represents player2
    Pilot pilot2;
    // the list of all targets for this world
    IList<Target> targets;
    // Targets for helicopter pieces
    Target t1;
    Target t2;
    Target t3;
    Target t4;
    Target t5;
    // helicopter for this world 
    Heli heli;
    // Scuba for the world
    ScubaTarget scuba;

    // constructor for the game
    ForbiddenIslandWorld() {
        this.waterHeight = 0;
        this.createRegularMountain();
        this.createPieces();
    }


    // draw this World
    public WorldScene makeScene() {
        // draw the cells
        WorldScene scene = this.getEmptyScene();
        //            System.out.println(board == null);
        for (Cell c: this.board) {
            //            System.out.println(c.height); 
            c.drawCell(c, scene, waterHeight);     
        }

        for (Target t: targets) {
            if (!t.isCollected) {
                scene.placeImageXY(t.target, t.loc.x * ForbiddenIslandWorld.CELL_SIZE + 5,
                        t.loc.y * ForbiddenIslandWorld.CELL_SIZE + 5);
            }
        }

        scene.placeImageXY(this.pilot.pilot,
                this.pilot.loc.x * ForbiddenIslandWorld.CELL_SIZE,
                this.pilot.loc.y * ForbiddenIslandWorld.CELL_SIZE);

        scene.placeImageXY(this.heli.image,
                this.heli.loc.x * ForbiddenIslandWorld.CELL_SIZE,
                this.heli.loc.y * ForbiddenIslandWorld.CELL_SIZE);

        scene.placeImageXY(new TextImage("Get to the Heli and win!", Color.red),
                ISLAND_HEIGHT * 3, ISLAND_HEIGHT * 3);

        scene.placeImageXY(new TextImage("Your SCORE:" + Integer.toString(score), Color.red),
                ISLAND_HEIGHT * 2, ISLAND_HEIGHT * 2);

        scene.placeImageXY(new TextImage("SwimTime:" + Integer.toString(pilot.scuba), Color.yellow),
                ISLAND_HEIGHT * 1, ISLAND_HEIGHT * 1);

        scene.placeImageXY(this.scuba.image,
                this.scuba.loc.x * ForbiddenIslandWorld.CELL_SIZE,
                this.scuba.loc.y * ForbiddenIslandWorld.CELL_SIZE);


        return scene;
    }

    // handle key events for this world
    public void onKeyEvent(String ke) {

        if (ke.equals("up") && !this.pilot.loc.top.isFlooded) {
            pilot.loc = pilot.loc.left;
            score += 1;
        }

        if (ke.equals("down") && !this.pilot.loc.bottom.isFlooded) {
            pilot.loc = pilot.loc.right;
            score += 1;
        }

        if (ke.equals("left") && !this.pilot.loc.left.isFlooded) {
            pilot.loc = pilot.loc.top;
            score += 1;
        }

        if (ke.equals("right") && !this.pilot.loc.right.isFlooded) {
            pilot.loc = pilot.loc.bottom;
            score += 1;
        }

        if (ke.equals("p")) {
            this.pilot.toggleScuba();
        }
        /*
         if (ke.equals("up") && !this.pilot2.loc.top.isFlooded) {
             pilot2.loc = pilot2.loc.left;
             score += 1;
         }

         if (ke.equals("down") && !this.pilot2.loc.bottom.isFlooded) {
             pilot2.loc = pilot2.loc.right;
             score += 1;
         }

         if (ke.equals("left") && !this.pilot2.loc.left.isFlooded) {
             pilot2.loc = pilot2.loc.top;
             score += 1;
         }

         if (ke.equals("right") && !this.pilot2.loc.right.isFlooded) {
             pilot2.loc = pilot2.loc.bottom;
             score += 1;
         }
         */

        if (ke.equals("q")) {
            this.pilot2.toggleScuba();
        }

        if (ke.equals("m")) {
            this.reset();

            this.createRegularMountain();
            this.createPieces();

        }
        else if (ke.equals("r")) {
            this.reset();

            this.createRandomMountain();
            this.createPieces();

        }
        else if (ke.equals("t")) {
            this.reset();

            this.createRandomTerrain();
            this.createPieces();

        }
    }


    // helper for mountainIsland(), randomTerrain(), and randomHeights()
    // creates an ArrayList<ArrayList<Double>> holding 0.0 to be modified in
    // the heights method
    ArrayList<ArrayList<Double>> makeArray() {
        ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();

        for (int ithRow = 0; ithRow <= ISLAND_SIZE; ithRow++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int col = 0; col <= ISLAND_SIZE; col++) {
                row.add(0.0);
            }
            result.add(row);
        }

        return result;
    }

    // create an ArrayList<ArrayList<Double>> representing symmetrical
    // heights for each cell in the world, creating a mountain island
    ArrayList<ArrayList<Double>> mountainIslandHeights() {
        int h = ISLAND_SIZE / 2;
        ArrayList<ArrayList<Double>> mountainHeights = this.makeArray();

        for (int ithRow = 0; ithRow <= ISLAND_SIZE; ithRow++) {
            ArrayList<Double> row = mountainHeights.get(ithRow);
            for (int col = 0; col <= ISLAND_SIZE; col++) {
                if (((Math.abs(h - col)) + (Math.abs(h - ithRow))) > h) {
                    row.set(col, 0.0);
                }
                else {
                    row.set(col,
                            (h - (double)((Math.abs(h - col)) + (Math.abs(h - ithRow)))));
                }
            }
        }
        return mountainHeights;
    }

    // create an ArrayList<ArrayList<Double>> representing randomly
    // generated heights for each cell in a diamond island 
    ArrayList<ArrayList<Double>> randomMountainHeights() {
        int h = ISLAND_SIZE / 2;
        ArrayList<ArrayList<Double>> mountainHeights = this.makeArray();

        for (int ithRow = 0; ithRow <= ISLAND_SIZE; ithRow++) {
            ArrayList<Double> row = mountainHeights.get(ithRow);
            for (int col = 0; col <= ISLAND_SIZE; col++) {
                if (((Math.abs(h - col)) + (Math.abs(h - ithRow))) > h) {
                    row.set(col, 0.0);
                }
                else {
                    row.set(col,
                            (rand.nextDouble() * (h - 1)) + 1);
                }
            }
        }
        return mountainHeights;
    }

    // helper for randomTerrainHelper()
    void randomTerrainHeightsHelper(ArrayList<ArrayList<Double>> heights,
            int minX, int minY, int maxX, int maxY, int quadrant) {

        double islandHeight = ISLAND_SIZE / 2;
        // the midpoints between the corners
        int midX = (minX + maxX) / 2;
        int midY = (minY + maxY) / 2;

        if (((minX + 2) > maxX) && ((minY + 2) > maxY)) {
            // base case is reached, do nothing
        }

        else {
            // tune these parameters
            double k = (5 * Math.random()) - (islandHeight / 4);
            // give a small chance for the cells to be below water height
            if ((Math.random() * 3) < 1) {
                k *= -1.5;
            }

            // the four corners heights
            double tl = heights.get(minY).get(minX);
            double tr = heights.get(minY).get(maxX);
            double bl = heights.get(maxY).get(minX);
            double br = heights.get(maxY).get(maxX);

            // compute the heights at the middle of each edge
            double l = Math.min(k * Math.random() + (tl + bl) / 2, islandHeight);
            double t = Math.min(k * Math.random() + (tl + tr) / 2, islandHeight);
            double r = Math.min(k * Math.random() + (tr + br) / 2, islandHeight);
            double b = Math.min(k * Math.random() + (bl + br) / 2, islandHeight);

            // replace heights at middles of edges with the values that
            // have been previously calculated, if they overlap with
            // a previously calculated value
            if (quadrant == 2) {
                l = heights.get(midY).get(minX);
            }
            else if (quadrant == 3) {
                t = heights.get(minY).get(midX);
            }
            else if (quadrant == 4) {
                l = heights.get(midY).get(minX);
                t = heights.get(minY).get(midX);
            }
            // assign the heights for the middles of the edges
            // left middle
            heights.get(midY).set(minX, l);
            // top middle
            heights.get(minY).set(minX, t);
            // right middle
            heights.get(midY).set(maxX, r);
            // bottom middle
            heights.get(maxY).set(midX, b);

            // compute the height at the middle of the quadrant
            double m = Math.min(k * Math.random() + (tl + tr + bl + br) / 4, islandHeight);

            // assign the height for the middle of the quadrant
            heights.get(midY).set(midX, m);

            // recur
            this.randomTerrainHeightsHelper(heights, minX, minY, midX, midY, 1);   // top left
            this.randomTerrainHeightsHelper(heights, minX, midY, midX, maxY, 2);  // top right
            this.randomTerrainHeightsHelper(heights, midX, minY, maxX, midY, 3);  // bottom left
            this.randomTerrainHeightsHelper(heights, midX, midY, maxX, maxY, 4); // bottom right
        }
    }


    // create an ArrayList<ArrayList<Double>> representing randomly
    // generated terrain with random heights for each cell in the World
    ArrayList<ArrayList<Double>> randomTerrainHeights() {

        int h = ISLAND_SIZE / 2;
        ArrayList<ArrayList<Double>> randomTerrain = this.makeArray();
        // set the center of the grid to the max island height
        randomTerrain.get(32).set(32, (double)h);
        // the corners of the grid have already been initialized to 0.0 based on
        // the function of this.makeArray()
        // set the middles of the four edges to 1 so they are just above the water
        // left middle
        randomTerrain.get(h).set(0, 1.0);
        // top middle
        randomTerrain.get(0).set(h, 1.0); 
        // right middle
        randomTerrain.get(h).set(ISLAND_SIZE, 1.0);
        // bottom middle
        randomTerrain.get(ISLAND_SIZE).set(h, 1.0);

        // compute the heights
        // top left, 1st quadrant
        this.randomTerrainHeightsHelper(randomTerrain, 0, 0, h, h, 1);
        // top right, 2nd quadrant
        this.randomTerrainHeightsHelper(randomTerrain, 0, h, h, ISLAND_SIZE, 2);
        // bottom left, 3rd quadrant
        this.randomTerrainHeightsHelper(randomTerrain, h, 0, ISLAND_SIZE, h, 3);
        // bottom right, 4th quadrant
        this.randomTerrainHeightsHelper(randomTerrain, h, h, ISLAND_SIZE, ISLAND_SIZE, 4);

        // smooth the terrain by averaging cells with nearby cells
        for (int row = 1; row < randomTerrain.size() - 1; row++) {
            for (int col = 1; col < randomTerrain.get(row).size() - 1; col++) {
                double l = randomTerrain.get(row).get(col - 1);
                double t = randomTerrain.get(row - 1).get(col);
                double r = randomTerrain.get(row).get(col + 1);
                double b = randomTerrain.get(row + 1).get(col);
                double x = randomTerrain.get(row).get(col);

                randomTerrain.get(row).set(col, (l + t + r + b + x) / 5);
            }
        }
        return randomTerrain;
    }

    // helper for cells
    // creates an ArrayList<ArrayList<Cell>> to represent the cells in the world
    // without assigning their neighbors yet. assuming cells don't have any neighbors
    // to start with lets us assign the required amount of neighbors
    // instead of later removing unnecessary neighbors
    ArrayList<ArrayList<Cell>> cellsHelp(ArrayList<ArrayList<Double>> heights) {
        ArrayList<ArrayList<Cell>> cells = new ArrayList<ArrayList<Cell>>();

        for (int ithRow = 0; ithRow <= ISLAND_SIZE; ithRow++) {
            ArrayList<Double> rowHeights = heights.get(ithRow);
            ArrayList<Cell> rowCells = new ArrayList<Cell>();
            for (int col = 0; col <= ISLAND_SIZE; col++) {
                if (rowHeights.get(col) < 1) {
                    rowCells.add(new OceanCell(ithRow, col));
                }
                else {
                    rowCells.add(new Cell(rowHeights.get(col), ithRow, col, false));
                }
            }
            cells.add(rowCells);
        }
        return cells;
    }

    // creates an ArrayList<ArrayList<Cell>> to represent the cells in the world
    // and assigns their neighbors, if they have them
    ArrayList<ArrayList<Cell>> cells(ArrayList<ArrayList<Double>> heights) {
        ArrayList<ArrayList<Cell>> cells = this.cellsHelp(heights);

        for (int row = 0; row <= ISLAND_SIZE; row++) {
            for (int col = 0; col <= ISLAND_SIZE; col++) {
                Cell c = cells.get(row).get(col);
                // assume this cell is neighbors with itself
                // to be later linked with the real neighbors
                // or to remain as linked to itself (if c is part of a border)
                Cell neighborLeft = c;
                Cell neighborTop = c;
                Cell neighborRight = c;
                Cell neighborBottom = c;


                // if the current cell is not on the left border,
                // assign it a neighborLeft that is one left of it
                if (col > 0) {
                    neighborLeft = cells.get(row).get(col - 1);
                }
                // if the current cell is not on the top border,
                // assign it a neighborTop that is one above it
                if (row > 0) {
                    neighborTop = cells.get(row - 1).get(col);
                }
                // if the current cell is not on the right border,
                // assign it a neighborRight that is one to the right of it
                if (col < cells.get(row).size() - 1) {
                    neighborRight = cells.get(row).get(col + 1);
                }
                // if the current cell is not on the bottom border,
                // assign it a neighborBottom that is one below it
                if (row < cells.size() - 1) {
                    neighborBottom = cells.get(row + 1).get(col);
                }

                // assign this cell's neighbors
                c.left = neighborLeft;
                c.top = neighborTop;
                c.right = neighborRight;
                c.bottom = neighborBottom;
            }
        }
        return cells;
    }

    // move the board of cells to the board, an IList<T>
    void transferCells(ArrayList<ArrayList<Cell>> cells) {
        this.board = new Mt<Cell>();
        ArrayList<Cell> currentRow = new ArrayList<Cell>();
        for (int ithRow = 0; ithRow <= ISLAND_SIZE; ithRow++) {
            currentRow = cells.get(ithRow);
            for (int col = 0; col <= ISLAND_SIZE; col++) {
                this.board = new Cons<Cell>(currentRow.get(col), this.board);
            }
        }
    }

    // create a list of all inland cells
    IList<Cell> inlandCells() {
        IList<Cell> inlandCells = new Mt<Cell>();
        for (Cell current : this.board) {
            if (!current.isFlooded &&
                    (!current.left.isFlooded ||
                            !current.right.isFlooded ||
                            !current.bottom.isFlooded ||
                            !current.top.isFlooded)) {
                inlandCells = inlandCells.add(current);
            }
        }
        return inlandCells;
    }

    // clear old cells, reset water height
    // create a regular mountain
    void createRegularMountain() {
        this.reset();
        // get the heights of a regular mountain
        ArrayList<ArrayList<Double>> heights = this.mountainIslandHeights();
        // create the cells
        //         System.out.println("Heights: " + heights.size());
        ArrayList<ArrayList<Cell>> cells = this.cells(heights);
        //       System.out.println("cells: " + cells);
        // move the cells to the board
        this.transferCells(cells);
    }

    // clear old cells, reset water height
    // create a mountain island of random heights
    void createRandomMountain() {
        this.reset();
        // get the heights of a mountain island with random heights
        ArrayList<ArrayList<Double>> heights = this.randomMountainHeights();
        // create the cells
        ArrayList<ArrayList<Cell>> cells = this.cells(heights);
        this.transferCells(cells);

    }

    // clear old cells, reset water height
    // create a randomly generated terrain
    void createRandomTerrain() {
        this.reset();
        // get the heights for the random terrain
        ArrayList<ArrayList<Double>> heights = this.randomTerrainHeights();
        // create the cells
        ArrayList<ArrayList<Cell>> cells = this.cells(heights);
        this.transferCells(cells);
    }

    // reset the water height
    void reset() {
        this.waterHeight = 0;
        this.timer = 0;
        this.score = 0;
        this.targetsCollected = 0;

    }



    // pick a random land cell
    public int randomInt() {
        return rand.nextInt(ForbiddenIslandWorld.ISLAND_SIZE);
    }

    // pick a random land cell
    public Cell randomCell() {
        int randX = rand.nextInt(ForbiddenIslandWorld.ISLAND_SIZE);
        int randY = rand.nextInt(ForbiddenIslandWorld.ISLAND_SIZE);

        // find cell with randX and randY
        for (Cell current : this.board) {
            if (current.x == randX && current.y == randY && !current.isFlooded) {
                return current;
            }
        }
        return this.randomCell();
    }


    // places the targets on the screen, on non-flooded cells
    public IList<Target> createTargets() {
        IList<Target> targets = new Mt<Target>();
        Cell cell;
        for (int i = 0; i < 5; i++) {
            cell = this.randomCell();
            targets = targets.add(new Target(cell));
        }
        return targets;
    }

    // randomly places the pilot on a non-flooded cell, with non-flooded neighbors
    public Pilot placePilot() {
        return new Pilot(this.randomCell());
    }

    public ScubaTarget placeScuba() {
        return new ScubaTarget(this.randomCell());
    }

    //update this.parts and player parts
    public void collection() {
        for (Target t : targets) {
            if (t.loc == this.pilot.loc) {
                this.targetsCollected += 1;
                this.targets = this.targets.remove(t);
            }
        }
    }

    //place the crashed helicopter on the highest cell on the board
    public Cell placeHeli() {
        Cell targetCell = null;
        double highest = 0;

        for (Cell c: this.board) {
            if (c.height > highest) {
                targetCell = c;
                highest = c.height;
            }
        }
        return targetCell;
    }

    // place the pilot, helicopter pieces, and include targets in the list of targets
    void createPieces() {
        this.heli = new Heli(this.placeHeli());
        this.targets = this.createTargets();
        this.pilot = this.placePilot();
        this.scuba = this.placeScuba();
    }



    //update the targets on every tick
    public void updateTargets() {
        for (Target t: targets) {
            if (pilot.loc.x == t.loc.x &&
                    pilot.loc.y == t.loc.y) {

                t.isCollected = true;
            }

        }
    }

    //floodIsland 
    //EFFECT: Flood the island and increase waterHeight on every 10 ticks
    void floodIsland() {
        for (Cell c: this.board) {
            c.floodCell(this.waterHeight);
        }
    }

    //on tick for the big bang bong
    public void onTick() {
        this.timer = this.timer + 1;
        if (this.timer % 10 == 0) {
            this.waterHeight = this.waterHeight + 1;
            this.floodIsland();  

        }
        this.updateTargets();
    }

    public boolean hasAllTargets() {
        for (Target t: targets) {
            if (!t.isCollected) {
                return false;
            }
        }
        return true;
    }

    //world ends for the big bang bong
    public WorldEnd worldEnds() {

        WorldEnd result = new WorldEnd(false, this.makeScene());
        if (this.waterHeight >= ISLAND_HEIGHT) {
            result = new WorldEnd(true, this.makeLossScene());
        }


        if (pilot.loc.isFlooded) {
            result = new WorldEnd(true, this.makeLossScene());
        }

        if (pilot.drowned()) {
            result = new WorldEnd(true, this.makeLossScene());
        }


        for (Target t: this.targets) {
            if (t.loc.isFlooded && !t.isCollected) {
                result = new WorldEnd(true, this.makeLossScene());
            }
        }


        if (this.hasAllTargets() && this.pilot.loc.x == this.heli.loc.x
                && this.pilot.loc.y == this.heli.loc.y) {
            result = new WorldEnd(true, this.makeWinScene());
        }

        return result;
    }

    // win scene for when the game ends
    public WorldScene makeWinScene() {
        WorldScene winScene = this.getEmptyScene();
        winScene.placeImageXY(
                new TextImage("YOU WIN!!!", 50, Color.RED),
                300, 300);
        return winScene;
    }

    // loss scene for when the game ends
    public WorldScene makeLossScene() {
        WorldScene lossScene = this.getEmptyScene();
        lossScene.placeImageXY(
                new TextImage("You lost :((", ISLAND_HEIGHT, Color.RED),
                300, 300);
        return lossScene;
    }

}


//an interface to hold Lists of T
interface IList<T> extends Iterable<T> {
    // is this list of T the same as that list of T?
    boolean sameList(IList<T> that);
    // is this Empty list of T the same as that EmptyList?
    boolean sameEmptyList(Mt<T> that);
    // is this Cons list of T the same as that ConsList?
    boolean sameConsList(Cons<T> that);
    // is this cons?
    boolean isCons();
    // is this empty?
    boolean isMT();
    // treat this IList as Cons
    Cons<T> asCons();
    // returns the size of this list
    int size();
    // remove the given T from this list
    IList<T> remove(T t);
    // add the given T to the front of this list
    IList<T> add(T t);
    // does this list contain the given T
    boolean has(T t);
}


//a class to represent ConsList<T>
class Cons<T> implements IList<T> {
    T first;
    IList<T> rest;
    Iterator<T> iterator;

    Cons(T first, IList<T> rest) {
        this.first = first;
        this.rest = rest;
    }

    // standard iterator over this
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }

    // is this list of T the same as that list?
    public boolean sameList(IList<T> that) {
        return that.sameConsList(this);
    }

    // is this list of T the same as that empty list?
    public boolean sameEmptyList(Mt<T> that) {
        return false;
    }

    // is this list of T the same as that cons list?
    public boolean sameConsList(Cons<T> that) {
        return this.first.equals(that.first) &&
                this.rest.sameList(that.rest);
    }

    // Is this a cons list?
    public boolean isCons() {
        return true;
    }

    // is this an empty list?
    public boolean isMT() {
        return false;
    }

    //return the size of this IList
    public int size() {
        return 1 + this.rest.size();
    }

    // treat this list as cons -> return this
    public Cons<T> asCons() {
        return this;
    }

    //does this list contain the given T
    public boolean has(T t) {
        return (t.equals(this.first)) || this.rest.has(t);
    }

    // remove the given T from this list
    public IList<T> remove(T t) {
        if (t == this.first) {
            return this.rest;
        }
        else {
            return new Cons<T>(this.first, this.rest.remove(t));
        }
    }

    // add the given item to the front of this list
    public IList<T> add(T t) {
        return new Cons<T>(t, this);
    }
}

//a class to represent MtList<T>
class Mt<T> implements IList<T> {

    // standard iterator over this
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }

    // is this list the same as that list?
    public boolean sameList(IList<T> that) {
        return that.sameEmptyList(this);
    }

    // is this list the same as that empty list?
    public boolean sameEmptyList(Mt<T> that) {
        return true;
    }

    // is this list the same as that cons list?
    public boolean sameConsList(Cons<T> that) {
        return false;
    }

    // Is this a cons list?
    public boolean isCons() {
        return false;
    }

    // is this an empty list?
    public boolean isMT() {
        return true;
    }

    //return the size of this IList
    public int size() {
        return 0;
    }

    // treat this list as cons -> return this
    public Cons<T> asCons() {
        throw new RuntimeException("You can't do this");
    }

    //does this list contain the given T
    public boolean has(T t) {
        return false;
    }

    // remove the given T from this list
    public IList<T> remove(T t) {
        return new Mt<T>();
    }

    // add the given item to the front of this list
    public IList<T> add(T t) {
        return new Cons<T>(t, this);
    }
}

class IListIterator<T> implements Iterator<T> {
    IList<T> loItems;


    IListIterator(IList<T> loItems) {
        this.loItems = loItems;
    }

    // cannot remove from the iter
    public void remove() {
        throw new UnsupportedOperationException("Don't do this!");
    }


    // does this item have a following item in the list?
    public boolean hasNext() {
        return this.loItems.isCons();
    }


    // move the iterator up one element in the list
    public T next() {
        Cons<T> itemsAsCons = this.loItems.asCons();
        T nextItem = itemsAsCons.first;
        this.loItems = itemsAsCons.rest;

        return nextItem;
    }


}


class ForbiddenIsland {
    // a new ForbiddenIsland Game 
    ForbiddenIslandWorld game;
    // the size of the board
    int size = ForbiddenIslandWorld.ISLAND_SIZE;
    // example cells
    Cell cell;
    Cell cell2;
    Cell cell3;
    Cell cell4;
    Cell cell5;
    // connecting a cell with its neighbors with the constructor
    Cell cell6;
    // an example OceanCell
    OceanCell ocean;
    OceanCell ocean2;


    // example cells
    Cell cell7;
    Cell cell8;
    Cell cell9;
    Cell cell10;
    // connecting a cell with its neighbors with the constructor
    Cell cell11;

    Cell cell21;
    Cell cell22;
    Cell cell23;
    Cell cell24;
    Cell cell20;

    Double double1;
    Double double2;
    Double double3;
    Double double4;
    ArrayList<Double> doubleSubList1;
    ArrayList<Double> doubleSubList2;
    ArrayList<ArrayList<Double>> doubleList1;
    ArrayList<Cell> cellSubList1;
    ArrayList<Cell> cellSubList2;
    ArrayList<Cell> cellSubList3;
    ArrayList<Cell> cellSubList4;
    ArrayList<Cell> cellSubList5;
    ArrayList<ArrayList<Cell>> cellList1;
    ArrayList<ArrayList<Cell>> cellList2;
    ForbiddenIslandWorld world1;

    ForbiddenIslandWorld world2;
    ForbiddenIslandWorld world3;


    Pilot pilot1;
    Target target1;
    Target target2;
    Target target3;
    Target target4;
    Target target5;
    ScubaTarget scuba;


    void init() {
        // a new ForbiddenIsland Game 
        ForbiddenIslandWorld game;
        // the size of the board
        int size = ForbiddenIslandWorld.ISLAND_SIZE;
        // example cells
        Cell cell = new Cell(16, 16, 16, false);
        Cell cell2 = new Cell(16, 15, 16, false);
        Cell cell3 = new Cell(16, 16, 17, false);
        Cell cell4 = new Cell(16, 17, 16, false);
        Cell cell5 = new Cell(16, 16, 15, false);
        // connecting a cell with its neighbors with the constructor
        Cell cell6 = new Cell(16, 16, 16, cell2, cell3,
                cell4, cell5, false);
        // an example OceanCell
        OceanCell ocean = new OceanCell(8, 8);
        OceanCell ocean2 = new OceanCell(16, 16, cell2, cell3, cell4, cell5);


        // example cells
        Cell cell7 = new Cell(17, 24, 25, false);
        Cell cell8 = new Cell(19, 25, 26, false);
        Cell cell9 = new Cell(19, 26, 25, false);
        Cell cell10 = new Cell(17, 25, 24, false);
        // connecting a cell with its neighbors with the constructor
        Cell cell11 = new Cell(18, 25, 25, cell7, cell8,
                cell9, cell10, false);

        Cell cell21 = new Cell(10, 12, 13, true);
        Cell cell22 = new Cell(10, 13, 14, true);
        Cell cell23 = new Cell(10, 14, 13, true);
        Cell cell24 = new Cell(10, 13, 12, true);
        Cell cell20 = new Cell(20, 13, 13, cell21, cell22, 
                cell23, cell24, false);


        Double double1 = 5.0;
        Double double2 = 9.0;
        Double double3 = 7.0;
        Double double4 = 15.2;
        ArrayList<Double> doubleSubList1 = new ArrayList<Double>();
        ArrayList<Double> doubleSubList2 = new ArrayList<Double>();
        ArrayList<ArrayList<Double>> doubleList1 = new ArrayList<ArrayList<Double>>();
        ArrayList<Cell> cellSubList1 = new ArrayList<Cell>();
        ArrayList<Cell> cellSubList2 = new ArrayList<Cell>();
        ArrayList<Cell> cellSubList3 = new ArrayList<Cell>();
        ArrayList<Cell> cellSubList4 = new ArrayList<Cell>();
        ArrayList<Cell> cellSubList5 = new ArrayList<Cell>();
        ArrayList<ArrayList<Cell>> cellList1 = new ArrayList<ArrayList<Cell>>();
        ArrayList<ArrayList<Cell>> cellList2 = new ArrayList<ArrayList<Cell>>();
        ForbiddenIslandWorld world1;
        ForbiddenIslandWorld world2;
        ForbiddenIslandWorld world3;


        Pilot pilot1 = new Pilot(cell6);
        Target target1 = new Target(cell);
        Target target2 = new Target(cell7);
        Target target3 = new Target(cell2);
        Target target4 = new Target(cell3);
        Target target5 = new Target(cell4);
        ScubaTarget scuba = new ScubaTarget(cell2);
    }


    // launch the game
    void testGame(Tester t) {
        this.game = new ForbiddenIslandWorld();
        //        game.createRegularMountain();
        game.bigBang(
                (ForbiddenIslandWorld.ISLAND_SIZE + 1) * ForbiddenIslandWorld.CELL_SIZE,
                (ForbiddenIslandWorld.ISLAND_SIZE + 1) * ForbiddenIslandWorld.CELL_SIZE,
                0.5);
    }

    
    // test onKeyEvent
    // failing for some reason but will fix with leena for pt 2
    // pls doge will test promise
    void testOnKeyEvent(Tester t) {
        // setup
        this.game = new ForbiddenIslandWorld();
        game.onKeyEvent("r");

        IList<Cell> testBoard = new Mt<Cell>();
        ArrayList<ArrayList<Cell>> cells = this.game.cells(this.game.mountainIslandHeights());
        ArrayList<Cell> currentRow = new ArrayList<Cell>();
        for (int ithRow = 0; ithRow <= this.size; ithRow++) {
            currentRow = cells.get(ithRow);
            for (int col = 0; col <= this.size; col++) {
                testBoard = new Cons<Cell>(currentRow.get(col), testBoard);
            }
        }


        t.checkExpect(this.game.board.sameList(testBoard), false);
        game.onKeyEvent("m");
        //             t.checkExpect(this.game.board.sameList(testBoard), true);

    }


    // test makeArray
    void testMakeArray(Tester t) {
        this.game = new ForbiddenIslandWorld();
        t.checkExpect(game.makeArray().size(), 65);
        t.checkExpect(game.makeArray().get(0).get(0), 0.0);
        t.checkExpect(game.makeArray().get(32).get(32), 0.0);
        t.checkExpect(game.makeArray().get(12).get(21), 0.0);
    }

    // test mountainIslandHeights
    void testMountainIslandHeights(Tester t) {
        this.game = new ForbiddenIslandWorld();
        t.checkExpect(game.mountainIslandHeights().size(), 65);
        t.checkExpect(game.mountainIslandHeights().get(0).get(0), 0.0);
        t.checkExpect(game.mountainIslandHeights().get(32).get(32), 32.0);
        t.checkExpect(game.mountainIslandHeights().get(12).get(21), 32.0 - ((32 - 21) + (32 - 12)));
    }

    // test randomIslandHeights
    void testRandomMountainHeights(Tester t) {
        this.game = new ForbiddenIslandWorld();
        ArrayList<ArrayList<Double>> heights = game.randomMountainHeights();
        int h = ForbiddenIslandWorld.ISLAND_SIZE / 2;

        for (int row = 0; row <= ForbiddenIslandWorld.ISLAND_SIZE; row++) {
            ArrayList<Double> ithRow = heights.get(row);
            for (int col = 0; col <= ForbiddenIslandWorld.ISLAND_SIZE; col++) {
                if ((Math.abs(row - h) + Math.abs(col - h)) > h) {
                    t.checkExpect(ithRow.get(col), 0.0);
                }
                else {
                    t.checkRange(
                            ithRow.get(col), 
                            1.0, 
                            32.0);
                }
            }
        }
    }

    // test random 
    void testRandomInt(Tester t) {
        this.game = new ForbiddenIslandWorld();
        t.checkRange(this.game.randomInt(), 0, 65);
    }


    // test randomTerrainHeights() and randomTerrainHeightsHelper()
    void testRandomTerrainHeights(Tester t) {
        this.game = new ForbiddenIslandWorld();
        int h = ForbiddenIslandWorld.ISLAND_SIZE / 2;
        // the lower and upper bounds
        double lowerBound = -1.5 * h;
        double upperBound = h;

        ArrayList<ArrayList<Double>> heights = game.randomMountainHeights();

        for (int row = 0; row <= ForbiddenIslandWorld.ISLAND_SIZE; row++) {
            ArrayList<Double> ithRow = heights.get(row);
            for (int col = 0; col <= ForbiddenIslandWorld.ISLAND_SIZE; col++) {
                t.checkRange(ithRow.get(col), lowerBound, upperBound);        
            }
        }
    }



    // test cells(ArrayList<ArrayList<Double>> heights)
    void testCells(Tester t) {
        this.game = new ForbiddenIslandWorld();
        ArrayList<ArrayList<Double>> heights = game.mountainIslandHeights();
        ArrayList<ArrayList<Cell>> cells = game.cells(heights);

        for (int r = 0; r < cells.size(); r++) {
            ArrayList<Cell> currentRow = cells.get(r);
            for (int c = 0; c < currentRow.size(); c++) {
                t.checkExpect(currentRow.get(c).height, heights.get(r).get(c));
                t.checkExpect(currentRow.get(c).x, r);
                t.checkExpect(currentRow.get(c).y, c);
            }
        }
    }

    // test cellsHelper(ArrayList<ArrayList<Double>> heights)
    void testCellsHelp(Tester t) {
        this.game = new ForbiddenIslandWorld();
        int h = ForbiddenIslandWorld.ISLAND_SIZE / 2;
        ArrayList<ArrayList<Double>> heights = this.game.mountainIslandHeights();
        ArrayList<ArrayList<Cell>> cells = this.game.cellsHelp(heights);

        for (int row = 0; row <= ForbiddenIslandWorld.ISLAND_SIZE; row++) {
            ArrayList<Cell> ithRow = cells.get(row);
            for (int col = 0; col <= ForbiddenIslandWorld.ISLAND_SIZE; col++) {
                if (((Math.abs(h - col)) + (Math.abs(h - row))) >= h) {
                    t.checkExpect(ithRow.get(col), new OceanCell(row, col));
                }
                else {
                    t.checkExpect(ithRow.get(col), 
                            new Cell((h - (double)((Math.abs(h - col)) + (Math.abs(h - row)))), 
                                    row, col, false));
                }
            }
        }
    }



    // test transferCells(ArrayList<ArrayList<Cells>> cells)
    void testTransferCells(Tester t) {
        this.game = new ForbiddenIslandWorld();
        this.game.board = new Mt<Cell>();
        t.checkExpect(this.game.board, new Mt<Cell>());
        ArrayList<ArrayList<Double>> heights = this.game.mountainIslandHeights();
        ArrayList<ArrayList<Cell>> cells = this.game.cells(heights);
        this.game.transferCells(cells);

        IList<Cell> testBoard = new Mt<Cell>();
        ArrayList<Cell> currentRow = new ArrayList<Cell>();
        for (int ithRow = 0; ithRow <= this.size; ithRow++) {
            currentRow = cells.get(ithRow);
            for (int col = 0; col <= this.size; col++) {
                testBoard = new Cons<Cell>(currentRow.get(col), testBoard);
            }
        }

        t.checkExpect(this.game.board.sameList(testBoard), true);
    }


    // test reset()
    // useful testing to be implemented upon implementation of flooding

    void testReset(Tester t) {
        this.game = new ForbiddenIslandWorld();
        t.checkExpect(this.game.waterHeight, 0);
        t.checkExpect(this.game.score, 0);
        t.checkExpect(this.game.targetsCollected, 0);
        this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            this.game.onTick();
        //            
        t.checkExpect(this.game.waterHeight, 0);
        //            t.checkExpect(this.game.timer, 18);
        //            t.checkExpect(this.game.score, 0);
        //            t.checkExpect(this.game.targetsCollected, 0);    
        //            this.game.reset();
        //            t.checkExpect(this.game.waterHeight, 0);
        //            t.checkExpect(this.game.timer, 0);
        //            t.checkExpect(this.game.score, 0);
        //            t.checkExpect(this.game.targetsCollected, 0);    
        //            
    }

    // test floodcell
    void testFloodcell(Tester t) {
        Cell cell21 = new Cell(10, 12, 13, true);
        Cell cell22 = new Cell(10, 13, 14, true);
        Cell cell23 = new Cell(10, 14, 13, true);
        Cell cell24 = new Cell(10, 13, 12, true);
        Cell cell20 = new Cell(20, 13, 13, cell21, cell22, 
                cell23, cell24, false);

        cell20.floodCell(10);
        t.checkExpect(cell20.isFlooded, false);
        t.checkExpect(cell20.isCoastline(), true);
        cell20.floodCell(15);
        t.checkExpect(cell20.isFlooded, false);
        cell20.floodCell(20);
        t.checkExpect(cell20.isFlooded, false);
        cell20.floodCell(25);
        t.checkExpect(cell20.isFlooded, true);


    }
    
//    // test floodIsland
//        void testFloodIsland(Tester t) {
//           this.init();
//           t.checkExpect(cell3.isFlooded, false);
//           this.game.floodIsland();
//           t.checkExpect(cell3.isFlooded, true);
//           
//        }
    
    // test createTargets
    void testCreateTargets(Tester t) {
        this.init();
        this.game.createTargets();
        t.checkExpect(this.game.targets.size(), 5);
        t.checkExpect(this.game.targets.isCons(), true);
        
    }
    
    // test createPieces
    void testCreatePieces(Tester t) {
        this.init();
        this.game.createPieces();
        t.checkRange(this.game.heli.loc.x, 0, 65);
        t.checkRange(this.game.heli.loc.y, 0, 65);
        t.checkRange(this.game.pilot.loc.x, 0, 65);
        t.checkRange(this.game.pilot.loc.y, 0, 65);
        t.checkExpect(this.game.targets.size(), 5);
        t.checkExpect(this.game.targets.isCons(), true);
    }
    
    
    // test PlaceHeli
    void testPlaceHeli(Tester t) {
        this.init();
        this.game.placeHeli();
        t.checkRange(this.game.pilot.loc.x, 0, 65);
        t.checkRange(this.game.pilot.loc.y, 0, 65);
        
    }
    
//    // test Collection
//    void testCollection(Tester t) {
//        this.init();
//        t.checkExpect(this.game.targets.size(), 5);
//        this.game.collection();
//        
//    }
    
    // test placePilot
    void testPlacePilot(Tester t) {
        this.init();
        this.game.placePilot();
        t.checkRange(this.game.pilot.loc.x, 0, 65);
        t.checkRange(this.game.pilot.loc.y, 0, 65);
        
    }

    //test ontick()
    void testOntick(Tester t) {
        this.init();

        this.game = new ForbiddenIslandWorld();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();
        this.game.onTick();

        t.checkExpect(this.game.waterHeight, 0);
        t.checkExpect(this.game.timer, 4);
        t.checkExpect(this.game.score, 0);
        t.checkExpect(this.game.targetsCollected, 0);    

    }

    // test worldEnds()
    void testWorldEnds(Tester t) {
        this.init();
        t.checkExpect(
                this.game.worldEnds().worldEnds, false);
        this.game.waterHeight = 100;
        t.checkExpect(
                this.game.worldEnds().worldEnds, true);
        this.init();
        this.game.waterHeight = 0;
        t.checkExpect(
                this.game.worldEnds().worldEnds, false);

    }
    
 // test the isCoastline() method
    void testIsCoastline(Tester t) {
        // example cells
        Cell cell = new Cell(16, 16, 16, false);
        Cell cell2 = new Cell(16, 15, 16, false);
        Cell cell3 = new Cell(16, 16, 17, true);
        Cell cell4 = new Cell(16, 17, 16, false);
        Cell cell5 = new Cell(16, 16, 15, false);
        Cell cell7 = new Cell(16, 16, 17, false);
        // connecting a cell with its neighbors with the constructor
        Cell cell6 = new Cell(16, 16, 16, cell2, cell3,
                cell4, cell5, false);
        Cell cell8 = new Cell(16, 16, 16, cell2, cell7,
                cell4, cell5, false);
        // an example OceanCell
        OceanCell ocean = new OceanCell(8, 8);
        OceanCell ocean2 = new OceanCell(16, 16, cell2, cell3, cell4, cell5);
        t.checkExpect(ocean2.isCoastline(), false);
        t.checkExpect(cell6.isCoastline(), true);
        t.checkExpect(cell8.isCoastline(), false);
    }

    
    // test randomCell()
    void testRandomCell(Tester t) {
        this.init();
        this.game = new ForbiddenIslandWorld();
        
        t.checkExpect(this.game.randomCell().isFlooded, false);
        
    }


    // test of terrain
    void testInlandCells(Tester t) {
        this.init();
        this.game = new ForbiddenIslandWorld();
        IList<Cell> landcells = this.game.inlandCells();
        
        t.checkExpect(landcells.asCons().first.isFlooded, false);

    }

    Mt<String> mt = new Mt<String>();
    IList<String> mary = new Cons<String>("Mary ",
            new Cons<String>("had ",
                    new Cons<String>("a ",
                            new Cons<String>("little ",
                                    new Cons<String>("lamb.", new Mt<String>())))));
    IList<String> mary2 = new Cons<String>("Mary ",
            new Cons<String>("had ",
                    new Cons<String>("a ",
                            new Cons<String>("little ",
                                    new Cons<String>("lamb.", 
                                            new Cons<String>("zoo", new Mt<String>()))))));
    IList<String> mary2length = new Cons<String>("zoo",
            new Cons<String>("Mary ",
                    new Cons<String>("had ",
                            new Cons<String>("a ",
                                    new Cons<String>("little ",
                                            new Cons<String>("lamb.", new Mt<String>()))))));
    IList<String> maryLexSorted = new Cons<String>("a ",
            new Cons<String>("had ",
                    new Cons<String>("lamb.",
                            new Cons<String>("little ",
                                    new Cons<String>("Mary ", mt)))));
    IList<String> maryLenSorted = new Cons<String>("a ",
            new Cons<String>("had ",
                    new Cons<String>("Mary ",
                            new Cons<String>("lamb.",
                                    new Cons<String>("little ", mt)))));

    Cons<String> los1 = new Cons<String>("tom", mt);
    Cons<String> los2 = new Cons<String>("yangsoo", los1);
    IList<String> los3 = new Cons<String>("On a",
            new Cons<String>("beach in",
                    new Cons<String>("hawaii", mt)));
    IList<String> los4 = new Cons<String>("alpha", mt);
    IList<String> los5 = new Cons<String>("alpha", mary);

    IList<String> los6 = new Cons<String>("alpha",
            new Cons<String>("beta",
                    new Cons<String>("alpha", mt)));
    IList<String> los7 = new Cons<String>("alpha",
            new Cons<String>("beta",
                    new Cons<String>("gamma", mt)));

    IList<String> los8 = new Cons<String>("fun",
            new Cons<String>("fundies", mt));

    IList<String> los9 = new Cons<String>("fundies",
            new Cons<String>("fun", mt));

    IList<String> los10 = new Cons<String>("gamma",
            new Cons<String>("beta",
                    new Cons<String>("alpha", mt)));
    IList<String> los11 = new Cons<String>("alpha",
            new Cons<String>("alpha",
                    new Cons<String>("alpha",
                            new Cons<String>("beta",
                                    new Cons<String>("beta",
                                            new Cons<String>("gamma", mt))))));
    IList<String> los12 = new Cons<String>("fun",
            new Cons<String>("fundies",
                    new Cons<String>("tom",
                            new Cons<String>("yangsoo", mt))));
    IList<String> los13 = new Cons<String>("beta",
            new Cons<String>("beta",
                    new Cons<String>("alpha",
                            new Cons<String>("alpha",
                                    new Cons<String>("alpha",
                                            new Cons<String>("gamma", mt))))));
    IList<String> los14 = new Cons<String>("tom",
            new Cons<String>("fun",
                    new Cons<String>("yangsoo",
                            new Cons<String>("fundies", mt))));



    String string1 = "aa";
    String string2 = "bb";
    String string3 = "cc";
    String string4 = "a";
    String string5 = "aaa";

    // tests the sameList(IList<String> los) method
    boolean testSameList(Tester t) {
        return t.checkExpect(this.mt.sameList(this.mt), true) &&
                t.checkExpect(this.mt.sameList(this.los1), false) &&
                t.checkExpect(this.los1.sameList(this.mt), false) &&
                t.checkExpect(this.los3.sameList(this.los3), true) &&
                t.checkExpect(this.los3.sameList(this.mary), false);
    }


    // tests the sameMtList(Mt<String> los) method
    boolean testSameMtList(Tester t) {
        return t.checkExpect(this.mt.sameEmptyList(this.mt), true) &&
                t.checkExpect(this.los1.sameEmptyList(this.mt), false);
    }


    // tests the sameConsList(Cons<String> los) method
    boolean testSameConsList(Tester t) {
        return t.checkExpect(this.mt.sameConsList(this.los1), false) &&
                t.checkExpect(this.los1.sameConsList(this.los2), false) &&
                t.checkExpect(this.los2.sameConsList(this.los2), true);
    }
    // below too be added to yangsoo
    // test the iterator for an IList
    void testIterator(Tester t) {
        IList<String> strings = new Cons<String>("hi", 
                new Cons<String>("wassup", new Mt<String>()));
        Iterator<String> iter = strings.iterator();
        t.checkExpect(iter.hasNext(), true);
        t.checkExpect(iter.next().equals("hi"), true);
    }

    // test size()
    void testSize(Tester t) {
        IList<String> mt = new Mt<String>();
        IList<String> strings = new Cons<String>("hi", new Cons<String>("wassup", mt));
        t.checkExpect(mt.size(), 0);
        t.checkExpect(strings.size(), 2);
    }

    // asCons()
    void testAsCons(Tester t) {
        IList<String> mt = new Mt<String>();
        IList<String> strings = new Cons<String>("hi", new Cons<String>("wassup", mt));
        t.checkExpect(strings.asCons().sameList(strings), true);
    }

    // test isCons()
    void testIsCons(Tester t) {
        IList<String> mt = new Mt<String>();
        IList<String> strings = new Cons<String>("hi", new Cons<String>("wassup", mt));
        t.checkExpect(mt.isCons(), false);
        t.checkExpect(strings.isCons(), true);
    }

    // test isMt()
    void testIsMt(Tester t) {
        IList<String> mt = new Mt<String>();
        IList<String> strings = new Cons<String>("hi", new Cons<String>("wassup", mt));
        t.checkExpect(mt.isMT(), true);
        t.checkExpect(strings.isMT(), false);
    }

    //Test of add(T) method for IList<T>
    void testAdd(Tester t) {
        IList<String> mt = new Mt<String>();
        IList<String> strings1 = new Cons<String>("wassup", mt);
        IList<String> strings2 = new Cons<String>("hi", new Cons<String>("wassup", mt));
        Iterator<String> iter = strings1.iterator();
        t.checkExpect(strings1.add("hi").sameList(strings2), true);
        t.checkExpect(mt.add("wassup").sameList(strings1), true);

    }


    //Test of has(T) method for IList<T>
    void testHas(Tester t) {
        IList<String> mt = new Mt<String>();
        IList<String> strings1 = new Cons<String>("wassup", mt);
        IList<String> strings2 = new Cons<String>("hi", new Cons<String>("wassup", mt));
        t.checkExpect(strings1.has("wassup"), true);
        t.checkExpect(strings2.has("hi"), true);
        t.checkExpect(mt.has("benLerner"), false);
    }

    //Test of remove(T) method for IList<T>
    void testRemove(Tester t) {
        IList<String> mt = new Mt<String>();
        IList<String> strings1 = new Cons<String>("wassup", mt);
        IList<String> strings2 = new Cons<String>("hi", new Cons<String>("wassup", mt));
        Iterator<String> iter = strings1.iterator();
        t.checkExpect(strings2.remove("hi").sameList(strings1), true);
        t.checkExpect(strings1.remove("wassup").sameList(mt), true);
    }

}