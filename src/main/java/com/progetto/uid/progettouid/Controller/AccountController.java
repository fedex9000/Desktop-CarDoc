package com.progetto.uid.progettouid.Controller;

import com.progetto.uid.progettouid.DataBase.Authentication;
import com.progetto.uid.progettouid.DataBase.DBConnection;
import com.progetto.uid.progettouid.DataBase.Validation;
import com.progetto.uid.progettouid.Message;
import com.progetto.uid.progettouid.Model.Product;
import com.progetto.uid.progettouid.Model.User;
import com.progetto.uid.progettouid.View.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AccountController {
    @FXML
    private Text addressText1, addressText2, alertPasswordText, balanceText1, balanceText2, changePasswordText, myOrderText, nameText1, nameText2, sendReportText, surnameText1, surnameText2, sendedText;
    @FXML
    private Text orderText1, orderText2, orderText3, orderText4, orderText5, orderText6;
    @FXML
    private Text orderNumber1, orderNumber2, orderNumber3, orderNumber4, orderNumber5, orderNumber6;
    @FXML
    private Text orderPrice1, orderPrice2, orderPrice3, orderPrice4, orderPrice5, orderPrice6;
    @FXML
    private TextField couponTextField, addressField;
    @FXML
    private Button changePasswordButton, sendReportButton, balanceButton, addressButton;
    @FXML
    private PasswordField passwordField, repeatPasswordField;
    @FXML
    private ScrollPane scrollPaneAccount;
    @FXML
    private TextArea sendReportArea;
    @FXML
    private VBox vBox1, vBox2, vBox3, vBox4, vBox5, vBox6;

    private Text[] orderTexts;

    private Text[] orderPrices;
    private VBox[] vBoxes;
    @FXML
    void addressButtonAction(ActionEvent event) { //metodo per inserire l'indirizzo di spedizione
        if (!addressField.getText().isEmpty()){ //controlliamo che il testo inserito non sia vuoto
            String address = addressField.getText(); //otteniamo il testo
            addressText2.setText(address); //impostiamo il nuovo indirizzo
            String email = Authentication.getInstance().getUser().email(); //otteniamo l'utente
            DBConnection.getInstance().updateAddress(email, address); //aggiorniamo all'interno del database attraverso email dell'utente il nuovo indirizzo
            addressField.clear();
        }else{
            SceneHandler.getInstance().showAlert("Errore", Message.address_error, 0);
        }
    }
    //metodo per inserire il coupon del saldo
    @FXML
    void balanceButtonAction(ActionEvent event) throws SQLException, ExecutionException, InterruptedException, TimeoutException {
        String coupon = couponTextField.getText();
        if (!coupon.isEmpty()){
            CompletableFuture<Boolean> future = DBConnection.getInstance().checkCoupon(coupon);  //controlla se il coupon inserito è presente nel database
            Boolean valid = future.get(10, TimeUnit.SECONDS);
            if (valid){ //se si
                String email = Authentication.getInstance().getUser().email();
                CompletableFuture<String> future1 = DBConnection.getInstance().getCouponValue(coupon); //otteniamo il valore del coupon
                String value = future1.get(10, TimeUnit.SECONDS);
                CompletableFuture<String> future2 = DBConnection.getInstance().getBalanceAccount(email); //otteniamo il saldo dell'account
                String actualBalance = future2.get(10, TimeUnit.SECONDS);
                value = value.replace(",",".");
                actualBalance = actualBalance.replace(",",".");
                double doubleActualBalance = Double.parseDouble(actualBalance);
                double doubleCouponBalance = Double.parseDouble(value);
                doubleActualBalance += doubleCouponBalance;
                String str = String.format("%.2f", doubleActualBalance).replace(".", ",");
                DBConnection.getInstance().updateBalance(email, str);
                balanceText2.setText(str+"€");
                SceneHandler.getInstance().showAlert("Coupon", Message.add_coupon_success, 1);
            }else{
                SceneHandler.getInstance().showAlert("Coupon", Message.add_coupon_error, 0);
            }
        }else{
            SceneHandler.getInstance().showAlert("Errore coupon", Message.add_coupon_error, 0);
        }
        couponTextField.clear();
    }

    //metodo per cambiare la password dell'account
    @FXML
    void changePasswordButtonAction(ActionEvent event) {
        String password = passwordField.getText(); // otteniamo stringa password
        String repeatPassword = repeatPasswordField.getText(); // idem
        String email = Authentication.getInstance().getUser().email(); //otteniamo email dell'utente
        boolean valid = Validation.getInstance().checkPassword(password, repeatPassword); // verifichiamo che le due password siano uguali
        String encryptedPassword = DBConnection.getInstance().encryptedPassword(password);  //cripdiamo la password
        if (valid) {
            DBConnection.getInstance().updatePassword(email, encryptedPassword); //aggiorniamo la nuova password dell'account all'inteno del database
            passwordField.clear();
            repeatPasswordField.clear();
            SceneHandler.getInstance().showAlert("Cambio password", Message.update_password_success, 1);
        }
    }

    //metodo per inviare una segnalazione
    @FXML
    void sendReportButtonAction(ActionEvent event) {
        if (!sendReportArea.getText().isEmpty()){
            createInfoThread();
        }
        sendReportArea.clear();
    }

    //thered che gestisce la stringa "segnalazione inviata" con durata di 1 secondo e mezza
    void createInfoThread(){
        Thread thread = new Thread(() -> {
            try {
                sendedText.setVisible(true);
                Thread.sleep(1500);
                sendedText.setVisible(false);
            } catch (InterruptedException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Message.thread_error, 0);
            }
        });
        thread.start();
    }


    private void arrayInitilize(){
        orderPrices = new Text[]{orderPrice1, orderPrice2, orderPrice3, orderPrice4, orderPrice5, orderPrice6};
        orderTexts = new Text[]{orderText1, orderText2, orderText3, orderText4, orderText5, orderText6};
        vBoxes = new VBox[]{vBox1, vBox2, vBox3, vBox4, vBox5, vBox6};
    }

    //metodo per caricare i dati dell'untente
    private void loadUserData() throws SQLException, ExecutionException, InterruptedException, TimeoutException {
        User user = Authentication.getInstance().getUser();//otteniamo l'utente
        nameText2.setText(user.name()); // settiamo il nome
        surnameText2.setText(user.surname()); //settiamo il cognome
        addressText2.setText(user.address()); //settiamo la via
        CompletableFuture<String> future = DBConnection.getInstance().getBalanceAccount(user.email()); //otteniamo il saldo dell'account dal databases
        String balance = future.get(10, TimeUnit.SECONDS);
        balanceText2.setText(balance+"€"); //settiamo il saldo ottenuto
        loadOrder(user.email()); // chiamiamo il metodo per caricare eventuali ordini collegati all'account
    }

    private void loadOrder(String email) throws SQLException, ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<ArrayList<ArrayList<Product>>> future = DBConnection.getInstance().getOrder(email);
        ArrayList<ArrayList<Product>> order = future.get(10, TimeUnit.SECONDS);
        CompletableFuture<ArrayList<String>> future1 = DBConnection.getInstance().getOrderNumber(email);
        ArrayList<String> ordersNumber = future1.get(10, TimeUnit.SECONDS);
        CompletableFuture<ArrayList<String>> future2 = DBConnection.getInstance().getOrderPrice(email);
        ArrayList<String> orderPrice = future2.get(10, TimeUnit.SECONDS);


        for(int i = 0; i < order.size(); i++){
            String orderConcatenate = "";
            for (int j = 0; j < order.get(i).size(); j++){
                CompletableFuture<Integer> future3 = DBConnection.getInstance().checkOrderProductQuantity(email, order.get(i).get(j).id(), ordersNumber.get(i));
                Integer quantity = future3.get(10, TimeUnit.SECONDS);
                orderConcatenate += order.get(i).get(j).name() + " " + "q.nta: " + quantity + "\n\n";
            }
            String price = orderPrice.get(i);
            price = price.replace(".", ",");
            orderPrices[i].setText(price + "€");
            orderTexts[i].setText(orderConcatenate);
            vBoxes[i].setVisible(true);
        }

    }
    @FXML
    void initialize() throws SQLException, ExecutionException, InterruptedException, TimeoutException {
        arrayInitilize();
        loadUserData();
    }
}
