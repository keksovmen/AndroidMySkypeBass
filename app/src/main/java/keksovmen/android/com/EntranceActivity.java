package keksovmen.android.com;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;

public class EntranceActivity extends AppCompatActivity implements LogicObserver {

    public final static String EXTRA_MESSAGE = "MESSAGE";
    private BaseApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        application = (BaseApplication) getApplication();
    }


    @Override
    protected void onStart() {
        super.onStart();
        application.setState(this);
    }

    public void connect(View view) {
        Object[] data = new String[3];
        data[0] = ((EditText) findViewById(R.id.name_field)).getText().toString();
        data[1] = ((EditText) findViewById(R.id.host_name_field)).getText().toString();
        data[2] = ((EditText) findViewById(R.id.port_field)).getText().toString();
        //add format checks
        application.handleRequest(BUTTONS.CONNECT, data);
    }

    @Override
    public void observe(ACTIONS actions, Object[] data) {
        switch (actions) {
            case CONNECT_SUCCEEDED:
                onConnect();
                break;
            case WRONG_HOST_NAME_FORMAT:
                BaseApplication.showDialog(this, "Wrong host name format - " + data[0] +
                        "\nMust be xxx.xxx.xxx.xxx where xxx is number from 0 to 255");
                break;
            case WRONG_PORT_FORMAT:
                BaseApplication.showDialog(this, "Wrong port format - " + data[0]);
                break;
            case PORT_OUT_OF_RANGE:
                BaseApplication.showDialog(this, "Port is out of range, must be in "
                        + data[0] + ". But yours is " + data[1]);
                break;
            case AUDIO_FORMAT_NOT_ACCEPTED:
                BaseApplication.showDialog(this, "Not connected, because your " +
                        "audio system can't handleRequest this format {" + " " + data[0] + " }");
                break;
        }
    }

    private void onConnect() {
        startActivity(new Intent(this, MainActivity.class));
    }

    //    // Метод обработки нажатия на кнопку
//    public void sendMessage(View view) {
//        // действия, совершаемые после нажатия на кнопку
//        // Создаем объект Intent для вызова новой Activity
//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        // Получаем текстовое поле в текущей Activity
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        // Получае текст данного текстового поля
//        String message = editText.getText().toString();
//        // Добавляем с помощью свойства putExtra объект - первый параметр - ключ,
//        // второй параметр - значение этого объекта
//        intent.putExtra(EXTRA_MESSAGE, message);
//        // запуск activity
//        startActivity(intent);
//    }

//    private boolean setTextCall(Message msg) {
//        String s = (String) msg.obj;
//        Log.i("VALUE", "Getted value = " + s);
//        EditText viewById = findViewById(R.id.connect_data);
//        viewById.setText(s);
//        return true;
//    }

//    public void connect(View view) {
//        new Thread(() -> {
//            try (Socket socket = new Socket() ){
//                socket.connect(new InetSocketAddress("37.146.158.216", 8188));
//                OutputStream outputStream = socket.getOutputStream();
//                byte[] buffer = new byte[]{0, 1, 0, 0, 0, 0, 0, 1};
//                outputStream.write(buffer);
//                Thread.sleep(1000);
//                InputStream inputStream = socket.getInputStream();
//                buffer = new byte[inputStream.available()];
//                int read = inputStream.read(buffer);
//                Log.i("Value", "Readed = " + read);
//                inputStream.close();
//                String s = new String(buffer, 8, read - 8, StandardCharsets.UTF_8);
//                Log.i("Value", "String = " + s);
//                handler.obtainMessage(1, s).sendToTarget();
////                obtain.sendToTarget();
//
////                EditText viewById = findViewById(R.id.connect_data);
////                viewById.setText(s);
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
}
