package com.example.demofx;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;


import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.*;


public class HelloApplication extends Application {

    private Circle circle;
    private StringBuffer currentBuffer = new StringBuffer();
    PrintWriter printWriter;
    ///private SerialPort comPort;
    private static Object lock=new Object();
    private PrintWriter pWriter;
    private BufferedReader bReader;
    private OutputStream outStream;
    private DataOutputStream m_outStream;


    @Override
    public void start(Stage stage) throws IOException {
        circle =new Circle(50);
        circle.setCenterX(100);
        circle.setCenterY(100);

        Button but1 = new Button();
        but1.setText("кнопка 1");
        but1.setOnAction(new ButtonListener());

        TableView<Player> playerTable = new TableView<>();
        Player player1 = new Player("vasa", 4, 5);
        Player player2 = new Player("petya", 3, 52);

        ObservableList<Player> playersList = FXCollections.observableArrayList();
        playersList.add(player1);
        playersList.add(player2);

        TableColumn<Player, String> nameColumn = new TableColumn<>("name");
        nameColumn.setMinWidth(200);
        //nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setCellFactory(TextFieldTableCell.<Player>forTableColumn());
        nameColumn.setEditable(true);

        TableColumn<Player, Integer> goalColumn = new TableColumn<>("goal");
        goalColumn.setMinWidth(50);
        goalColumn.setCellValueFactory(new PropertyValueFactory<>("goal"));

        TableColumn<Player, Integer> failedColumn = new TableColumn<>("failed");
        failedColumn.setMinWidth(50);
        failedColumn.setCellValueFactory(new PropertyValueFactory<>("failed"));

        playerTable.setItems(playersList);
        playerTable.getColumns().addAll(nameColumn, goalColumn, failedColumn);

        Label lbl = new Label();

        TableView.TableViewSelectionModel<Player> selectionModel = playerTable.getSelectionModel();
        playerTable.getSelectionModel().setCellSelectionEnabled(true);
        playerTable.setEditable(true);
        playerTable.getSelectionModel().selectFirst();
//        selectionModel.selectedItemProperty().addListener(new ChangeListener<Player>(){
//
//            public void changed(ObservableValue<? extends Player> val, Player oldVal, Player newVal){
//                if(newVal != null) lbl.setText("Selected: " + newVal.getName());
//            }
//        });
        VBox pane = new VBox();
        pane.getChildren().add(circle);
        pane.getChildren().add(but1);
        pane.getChildren().add(playerTable);
        pane.getChildren().add(lbl);
        Scene scene = new Scene(pane,400, 400);
        stage.setScene(scene);
        //stage.setFullScreen(true);
        stage.show();

//        System.out.println("con ports length " + SerialPort.getCommPorts().length);
//        for( SerialPort port: SerialPort.getCommPorts()) {
//            System.out.println("baudRate "+port.getBaudRate()+" SysPortName "+port.getSystemPortName() +" portPath "+port.getSystemPortPath()+
//                    " stopBit "+port.getNumStopBits()+" dataBit "+port.getNumDataBits());
//        }
        //  comPort = SerialPort.getCommPorts()[0];

        //comPort.openPort();
      //  printWriter = new PrintWriter(comPort.getOutputStream());


        //Create a UUID for SPP
        UUID uuid = new UUID("1101", true);
        //Create the servicve url
        String connectionString = "btspp://localhost:" + uuid +";name=Sample SPP       Server";

        //open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open( connectionString );

        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        StreamConnection connection = streamConnNotifier.acceptAndOpen();

        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: "+dev.getBluetoothAddress());
        System.out.println("Remote device name: "+dev.getFriendlyName(true));

        InputStream inStream=connection.openInputStream();
        bReader=new BufferedReader(new InputStreamReader(inStream));


        m_outStream = new DataOutputStream(connection.openOutputStream());




//        comPort.addDataListener(new SerialPortDataListener() {
//            @Override
//            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
//            @Override
//            public void serialEvent(SerialPortEvent event)
//            {
//                byte[] newData = event.getReceivedData();
//                for (int i = 0; i < newData.length; ++i){
//                    currentBuffer.append((char)newData[i]);
//                }
//                System.out.print(currentBuffer.toString());
//                currentBuffer.delete(0, currentBuffer.length());
//            }
//        });
        new Thread(() -> {
            while(true) {
                try {
                    System.out.println(bReader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();


    }

    public static void main(String[] args) {
        launch();
    }

    class ButtonListener implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {
            System.out.println("---send---");
            try {
                m_outStream.writeUTF("poiu");
                m_outStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



    public class Player{

        public Player() {
        }

        public Player(String name, Integer goal, Integer failed) {
            this.name.set(name);
            this.goal.set(goal);
            this.failed.set(failed);
        }
        private SimpleStringProperty name = new SimpleStringProperty();
        private SimpleIntegerProperty goal = new SimpleIntegerProperty();
        private SimpleIntegerProperty failed = new SimpleIntegerProperty();

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public int getGoal() {
            return goal.get();
        }

        public SimpleIntegerProperty goalProperty() {
            return goal;
        }

        public void setGoal(int goal) {
            this.goal.set(goal);
        }

        public int getFailed() {
            return failed.get();
        }

        public SimpleIntegerProperty failedProperty() {
            return failed;
        }

        public void setFailed(int failed) {
            this.failed.set(failed);
        }
    }
}