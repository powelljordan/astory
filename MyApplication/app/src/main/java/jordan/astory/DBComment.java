package jordan.astory;

/**
 * Created by Jordan on 1/13/2016.
 */
public class DBComment {
    String message;
    String author;

    public DBComment(){

    }

    public DBComment(String author, String message){
        this.message = message;
        this.author = author;
    }

    public String getMessage(){
        return message;
    }

    public String getAuthor(){
        return author;
    }
}
