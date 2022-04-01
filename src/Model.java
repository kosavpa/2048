import java.util.*;

public class Model {
    private Tile[][] gameTiles;
    private static final int FIELD_WIDTH = 4;
    int maxTile = 0;
    int score = 0;
    private boolean isSaveNeeded = true;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();

    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private List<Tile> getEmptyTiles(){
        List<Tile> emptyTiles = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                Tile tiles = gameTiles[i][j];
                if(tiles.isEmpty())
                    emptyTiles.add(tiles);
            }
        }
        return emptyTiles;
    }

    void addTile(){
        List<Tile> tiles = getEmptyTiles();
        if(!tiles.isEmpty() && tiles != null) {
            Tile tile = tiles.get((int) (Math.random() * tiles.size())% tiles.size());
            tile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    void resetGameTiles(){
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }

        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean isChanges = false;
        int insertPosition = 0;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    isChanges = true;
                }
                insertPosition++;
            }
        }
        return isChanges;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isChanges = false;
        LinkedList<Tile> tilesList = new LinkedList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].isEmpty()) {
                continue;
            }

            if (i < FIELD_WIDTH - 1 && tiles[i].value == tiles[i + 1].value) {
                int updatedValue = tiles[i].value * 2;
                if (updatedValue > maxTile) {
                    maxTile = updatedValue;
                }
                score += updatedValue;
                tilesList.addLast(new Tile(updatedValue));
                tiles[i + 1].value = 0;
                isChanges = true;
            } else {
                tilesList.addLast(new Tile(tiles[i].value));
            }
            tiles[i].value = 0;
        }

        for (int i = 0; i < tilesList.size(); i++) {
            tiles[i] = tilesList.get(i);
        }
        return isChanges;
    }

    void left(){
        if(isSaveNeeded)
            saveState(gameTiles);
        boolean addedTile = false;
        for(Tile[] gameTilesX : gameTiles){
            if(compressTiles(gameTilesX) | mergeTiles(gameTilesX))
                addedTile = true;
        }
        if(addedTile)
            addTile();
        isSaveNeeded = true;
    }

    void rotate(Tile[][] array){
        Tile[][] newArray = new Tile[array.length][array.length];

        for(int y = 0; y < array.length; y++){
            for(int x = 0; x < array.length; x++){
                newArray[x][array.length - 1 - y] = array[y][x];
            }
        }

        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array.length; j++){
                array[i][j] = newArray[i][j];
            }
        }
    }

    void down(){
        saveState(gameTiles);
        rotate(gameTiles);
        left();
        rotate(gameTiles);
        rotate(gameTiles);
        rotate(gameTiles);
    }

    void up(){
        saveState(gameTiles);
        rotate(gameTiles);
        rotate(gameTiles);
        rotate(gameTiles);
        left();
        rotate(gameTiles);
    }

    void right(){
        saveState(gameTiles);
        rotate(gameTiles);
        rotate(gameTiles);
        left();
        rotate(gameTiles);
        rotate(gameTiles);
    }

    boolean canMove(){
        if(getEmptyTiles().size() != 0){
            return true;
        }

        for(int y = 0; y < FIELD_WIDTH; y++){
            for(int x = 0; x < FIELD_WIDTH; x++){
                Tile currentTile = gameTiles[y][x];
                if(((y < FIELD_WIDTH - 1) && currentTile.value == gameTiles[y+1][x].value) ||
                        ((x < FIELD_WIDTH - 1) && currentTile.value == gameTiles[y][x+1].value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] tempTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tempTiles[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(tempTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.isEmpty() && !previousScores.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void randomMove(){
        int random = (int)(Math.random() * 100) % 4;
        switch (random){
            case 0 : left(); break;
            case 1 : down(); break;
            case 2 : up(); break;
            default : right();
        }
    }

    MoveEfficiency getMoveEfficiency(Move move){

        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);

        move.move();

        if(hasBoardChanged()){
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();

        return moveEfficiency;
    }

    private boolean hasBoardChanged() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value != previousStates.peek()[i][j].value) {
                    return true;
                }
            }
        }
        return false;
    }

    void autoMove(){
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());

        queue.offer(getMoveEfficiency(() -> left()));
        queue.offer(getMoveEfficiency(() -> left()));
        queue.offer(getMoveEfficiency(() -> up()));
        queue.offer(getMoveEfficiency(() -> down()));

        queue.poll().getMove().move();
    }
}
