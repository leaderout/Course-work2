package com.cmbpizza.razor.golubev;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginScreen extends Activity implements View.OnClickListener, GestureDetector.OnGestureListener{

    Button LoginAccessButton, MainLoginButton, RegisterButton;
    AutoCompleteTextView LoginEmail;
    EditText FirstName, LastName, Mobile, Email, Password, ConfirmPassword, LoginPassword;
    ImageView LoginBackgroundImage, LoginBottomImage;
    GestureDetector gestureDetector;
    private static SQLLiteHelperProducts sqLiteHelper;
    public Dialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initializeComponents();
        initializeListeners();
        initializeAnimators();
        initializeGestures();
        sqlLiteDB();
        setBackgroundImage();
    }

    private void initializeComponents(){
        LoginAccessButton = findViewById(R.id.btnLogin);
        RegisterButton = findViewById(R.id.btnRegister);
        LoginBottomImage = findViewById(R.id.login_bottom_image);
        LoginBackgroundImage = findViewById(R.id.loginBackgroundImage);

        FirstName = findViewById(R.id.txtFirstName);
        LastName = findViewById(R.id.txtLastName);
        Mobile = findViewById(R.id.txtMobileNumber);
        Email = findViewById(R.id.txtEmail);
        Password = findViewById(R.id.txtPassword);
        ConfirmPassword = findViewById(R.id.txtConfirmPassword);

        loginDialog = new Dialog(LoginScreen.this);
        loginDialog.setContentView(R.layout.login_dialog_screen);
        MainLoginButton = loginDialog.findViewById(R.id.btnLoginMain);
        LoginEmail = loginDialog.findViewById(R.id.txtLoginEmail);
        LoginPassword = loginDialog.findViewById(R.id.txtLoginPassword);
        loginDialog.setCancelable(true);
        loginDialog.setCanceledOnTouchOutside(true);
    }

    private void initializeAnimators(){
        Drawable drawable =  LoginBottomImage.getDrawable();
        if(drawable instanceof Animatable){
            ((Animatable)drawable).start();
        }
    }

    private void initializeListeners(){
        LoginAccessButton.setOnClickListener(this);
        RegisterButton.setOnClickListener(this);
        MainLoginButton.setOnClickListener(this);
    }

    private void initializeGestures(){
        gestureDetector = new GestureDetector(LoginScreen.this ,this);
        findViewById(R.id.login_bottom_image).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    private void setBackgroundImage(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(),R.drawable.main_background),size.x,size.y,true);

        LoginBackgroundImage.setImageBitmap(bmp);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnLogin:
                loginDialog.show();
                break;
            case R.id.btnRegister:
                registerUser();
                break;
            case R.id.btnLoginMain:
                checkUser();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent end, float v, float v1) {
        if(start.getY() > end.getY()){
            Intent newIntent = new Intent(LoginScreen.this, UserMenu.class);
            startActivity(newIntent);
            overridePendingTransition(R.anim.layout_slide_up, R.anim.back_layout_slide_up);
            return true;
        } else if(start.getX() > end.getX() || start.getY() < end.getY() || start.getX() < end.getX()){
            return false;
        } else{
            return false;
        }
    }

    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(LoginScreen.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS userTable (userId INTEGER PRIMARY KEY AUTOINCREMENT, userFirstName VARCHAR, userLastName VARCHAR, userMobile VARCHAR, userEmail VARCHAR, userPassword VARCHAR)");

    }

    private void checkUser(){
        String AdminEmail = "admin";
        String AdminPass = "pass";
        if(inputLoginValidation()){
            String Email = LoginEmail.getText().toString();
            String Password = LoginPassword.getText().toString();
            if(Email.equals(AdminEmail) && Password.equals(AdminPass)){
                Intent newIntent = new Intent(LoginScreen.this, AdminMenu.class);
                startActivity(newIntent);
                clearEditTextValues();
                loginDialog.cancel();
            } else {
                if(sqLiteHelper.checkEmail(Email)){
                    if(sqLiteHelper.checkPassword(Email, Password)){
                        int UserId = sqLiteHelper.getUserId(Email);
                        Intent newIntent = new Intent(LoginScreen.this, UserMenu.class);
                        newIntent.putExtra("userId", UserId);
                        startActivity(newIntent);
                        clearEditTextValues();
                        loginDialog.cancel();
                    } else {
                        Toast.makeText(LoginScreen.this, "Ваш пароль неверен, пожалуйста, попробуйте еще раз", Toast.LENGTH_SHORT).show();
                        LoginPassword.requestFocus();
                    }
                } else {
                    Toast.makeText(LoginScreen.this, "Ваша электронная почта не существует", Toast.LENGTH_SHORT).show();
                    LoginEmail.requestFocus();
                }
            }
        }
    }

    private void clearEditTextValues(){
        LoginEmail.getText().clear();
        LoginPassword.getText().clear();
    }

    private void registerUser(){
        if(inputValidation()){
            String UserFirstName = FirstName.getText().toString();
            String UserLastName = LastName.getText().toString();
            String UserMobile = Mobile.getText().toString();
            String UserEmail = Email.getText().toString();
            String UserPassword = Password.getText().toString();
            sqLiteHelper.registerUser(UserFirstName, UserLastName, UserMobile, UserEmail, UserPassword);
            Toast.makeText(this, "Регистрация Успешна", Toast.LENGTH_SHORT).show();
            clearRegistrationForm();
            int UserId = sqLiteHelper.getUserId(UserEmail);
            Intent newIntent = new Intent(LoginScreen.this, UserMenu.class);
            newIntent.putExtra("userId", UserId);
            startActivity(newIntent);
            overridePendingTransition(R.anim.layout_slide_up, R.anim.back_layout_slide_up);
        }
    }

    private void clearRegistrationForm() {
        FirstName.getText().clear();
        LastName.getText().clear();
        Mobile.getText().clear();
        Email.getText().clear();
        Password.getText().clear();
        ConfirmPassword.getText().clear();
    }

    private boolean inputValidation() {
        boolean valid;
        String UserFirstName = FirstName.getText().toString();
        String UserLastName = LastName.getText().toString();
        String UserMobile = Mobile.getText().toString();
        String UserEmail = Email.getText().toString();
        String UserPassword = Password.getText().toString();
        String UserConfirmPassword = ConfirmPassword.getText().toString();
        if (UserFirstName.isEmpty()) {
            valid = false;
            FirstName.requestFocus();
            Toast.makeText(this, "Пожалуйста, введите свое имя", Toast.LENGTH_SHORT).show();
        } else if (UserLastName.isEmpty()) {
            valid = false;
            LastName.requestFocus();
            Toast.makeText(this, "Пожалуйста, введите свою фамилию", Toast.LENGTH_SHORT).show();
        } else if (UserMobile.isEmpty()) {
            valid = false;
            Mobile.requestFocus();
            Toast.makeText(this, "Пожалуйста, введите номер мобильного телефона", Toast.LENGTH_SHORT).show();
        } else if (UserEmail.isEmpty()) {
            valid = false;
            Email.requestFocus();
            Toast.makeText(this, "Пожалуйста, введите свой адрес электронной почтыs", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(UserEmail).matches()) {
            valid = false;
            LoginEmail.requestFocus();
            Toast.makeText(this, "Пожалуйста, введите действительный адрес электронной почты", Toast.LENGTH_SHORT).show();
        } else if (UserPassword.isEmpty()) {
            valid = false;
            Password.requestFocus();
            Toast.makeText(this, "Пожалуйста, укажите пароль", Toast.LENGTH_SHORT).show();
        } else if (UserConfirmPassword.isEmpty()) {
            valid = false;
            ConfirmPassword.requestFocus();
            Toast.makeText(this, "Повторно введите свой пароль в поле подтвердить пароль", Toast.LENGTH_SHORT).show();
        } else if (!UserPassword.equals(UserConfirmPassword)) {
            valid = false;
            ConfirmPassword.requestFocus();
            Toast.makeText(this, "Ваше подтверждение пароля не совпадает с вашим паролем", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }
        return valid;
    }

    private boolean inputLoginValidation() {
        boolean valid;
        String Email = LoginEmail.getText().toString();
        String Password = LoginPassword.getText().toString();
        if (String.valueOf(Email).isEmpty()) {
            valid = false;
            LoginEmail.requestFocus();
            Toast.makeText(this, "Пожалуйста, введите свой адрес электронной почты", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(Password).isEmpty()) {
            valid = false;
            LoginPassword.requestFocus();
            Toast.makeText(this, "Пожалуйста, введите свой пароль", Toast.LENGTH_SHORT).show();
        } else if(Email.equals("admin")) {
            valid = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            valid = false;
            LoginEmail.requestFocus();
            Toast.makeText(this, "Пожалуйста, введите действительный адрес электронной почты", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }

        return valid;
    }
}
