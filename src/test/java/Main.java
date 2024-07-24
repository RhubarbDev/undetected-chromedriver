import Utils.PatcherUtil;
import com.google.gson.JsonObject;

public class Main {

    public static void main(String[] args) {
        JsonObject obj = PatcherUtil.getJson();
        System.out.println(obj.toString());
    }
}
