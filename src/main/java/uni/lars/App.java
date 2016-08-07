package uni.lars;

public class App
{
    public static void main( String[] args ) {
        Indexer index = new Indexer(false);
        Searcher searcher = new Searcher();

        searcher.lookup("com.kiwigrid*");
    }
}
