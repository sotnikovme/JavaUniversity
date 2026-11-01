package crud;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import model.User;

public class UserDbInteraction {

    public User user;

    public void UserDbInteraction(User user){
        this.user = user;
    }

    public int addUserInDb(User user) throws IOException {
        try (
            FileOutputStream out = new FileOutputStream("db/users.txt", true);
        ){
            byte[] paylodInBytes = (user.toString() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            out.write(paylodInBytes);;
        }
        return 0;
    }

    public int addUserInDbAsCurrent(){

        return 0;
    }


    public void giveRole(){
        
    }

}