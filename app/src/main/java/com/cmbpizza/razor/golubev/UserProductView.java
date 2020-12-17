package com.cmbpizza.razor.golubev;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

public class UserProductView extends Activity implements AppCompatCallback, View.OnClickListener {

    private static SQLLiteHelperProducts sqLiteHelper;
    public int productID;
    public static boolean isUserLoggedIn = false;
    String randomCartID;
    TextView ProductTitle, ProductDescription, ProductPrice;
    Button NumberStepperUp, NumberStepperDown, AddToCart;
    TextView NumberStepperView;
    EditText NumberStepperEdit;
    ImageView ProductImage;
    ViewSwitcher ViewSwitcherNumberStepper;
    Toolbar toolbar;
    ArrayList<Products> ProductList;
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        delegateAppcompat(savedInstanceState);
        initializeComponents();
        initializeListeners();
        sqlLiteDB();
        fillData();
        checkLoginStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLoginStatus();
    }

    private void delegateAppcompat(Bundle savedInstanceState) {
        AppCompatDelegate delegate;
        delegate = AppCompatDelegate.create(this, this);

        delegate.onCreate(savedInstanceState);

        delegate.setContentView(R.layout.activity_user_product_view);

        toolbar = findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
    }

    public void initializeComponents() {
        ProductList = new ArrayList<>();
        ProductImage = findViewById(R.id.imgUserViewProductImage);
        ProductTitle = findViewById(R.id.txtUserProductTitle);
        ProductDescription = findViewById(R.id.txtUserProductDescription);
        ProductPrice = findViewById(R.id.txtUserProductPrice);
        productID = Integer.parseInt(getIntent().getExtras().get("productId").toString());
        randomCartID = getIntent().getExtras().get("cartId").toString();
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapseToolbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandToolbar);
        ViewSwitcherNumberStepper = findViewById(R.id.viewSwitcherNumberStepper);
        AddToCart = findViewById(R.id.btnAddProductToCart);
        NumberStepperUp = findViewById(R.id.btnNumberStepperUp);
        NumberStepperDown = findViewById(R.id.btnNumberStepperDown);
        NumberStepperEdit = findViewById(R.id.txtNumberStepperEdit);
        NumberStepperView = findViewById(R.id.txtNumberStepperView);
        NumberStepperView.setText("1");
        NumberStepperEdit.setText("1");
    }

    public void initializeListeners() {
        NumberStepperUp.setOnClickListener(this);
        NumberStepperDown.setOnClickListener(this);
        NumberStepperView.setOnClickListener(this);
        AddToCart.setOnClickListener(this);

        NumberStepperEdit.addTextChangedListener(new TextWatcher() {
            private String value = NumberStepperEdit.getText().toString();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                value = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                if (String.valueOf(charSequence).isEmpty()) {
                    NumberStepperEdit.setText(value);
                } else {
                    int numValue = Integer.valueOf(charSequence.toString());
                    if (!(numValue > 0 && numValue <= 30)) {
                        NumberStepperEdit.setText(value);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void checkLoginStatus() {
        isUserLoggedIn = getIntent().getExtras().get("userId") != null;
    }

    public void fillData() {
        ProductList = sqLiteHelper.getAllProductData(productID);
        toolbar.setTitle(ProductList.get(0).getProductCategory() + " | " + ProductList.get(0).getProductTitle());
        ProductTitle.setText(ProductList.get(0).getProductTitle());
        ProductDescription.setText(ProductList.get(0).getProductDescription());
        int productPrice = ProductList.get(0).getProductPrice();
        String priceText = getResources().getString(R.string.txtProductPricePrefix, productPrice);
        ProductPrice.setText(priceText);

        byte[] productImageByte = ProductList.get(0).getProductImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        ProductImage.setImageBitmap(imageBitmap);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(UserProductView.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productTable (productId INTEGER PRIMARY KEY AUTOINCREMENT, productTitle VARCHAR, productCategory VARCHAR, productPrice INTEGER, productDescription VARCHAR, productImage BLOB)");
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productCartTable (cartId VARCHAR PRIMARY KEY, productCount INTEGER, idList VARCHAR, titleList INTEGER, priceList VARCHAR, quantityList VARCHAR, totalPrice INTEGER)");
    }

    private void NumberSwitcher(char sign) {
        int number = 0;
        if (ViewSwitcherNumberStepper.getCurrentView().equals(NumberStepperView)) {
            number = Integer.valueOf(NumberStepperView.getText().toString());
        } else if (ViewSwitcherNumberStepper.getCurrentView().equals(NumberStepperEdit)) {
            if (String.valueOf(NumberStepperEdit.getText().toString()).isEmpty()) {
                number = 0;
            } else {
                number = Integer.valueOf(NumberStepperEdit.getText().toString());
            }
            ViewSwitcherNumberStepper.showNext();
        }

        if (sign == '+') {
            if (number < 30) {
                number++;
            }
        } else if (sign == '-') {
            if (number > 1) {
                number--;
            }
        }

        String numValue = String.valueOf(number);
        NumberStepperView.setText(numValue);
        NumberStepperEdit.setText(numValue);
    }

    private void addProductToCart() {
        String EditValue = NumberStepperEdit.getText().toString();
        String ViewValue = NumberStepperView.getText().toString();

        if(isUserLoggedIn){
            int productQuantity = Integer.valueOf(EditValue);
            if (!String.valueOf(EditValue).isEmpty()) {
                if (ViewValue.equals(EditValue)) {
                    sqLiteHelper.createCart(randomCartID, productID, productQuantity);
                }
            }
        } else {
            Toast.makeText(this, "Пожалуйста зарегистрируйтесь или авторизируйтесь, чтобы добавить продукт", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtNumberStepperView:
                ViewSwitcherNumberStepper.showNext();
                NumberStepperEdit.requestFocus();
                break;
            case R.id.btnNumberStepperUp:
                NumberSwitcher('+');
                break;
            case R.id.btnNumberStepperDown:
                NumberSwitcher('-');
                break;
            case R.id.btnAddProductToCart:
                addProductToCart();
                finish();
                break;
        }
    }
}
