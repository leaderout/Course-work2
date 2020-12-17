package com.cmbpizza.razor.golubev;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

public class AdminProductView extends Activity implements View.OnClickListener {

    private static SQLLiteHelperProducts sqLiteHelper;
    final int REQUEST_CODE_GALLERY = 999;
    public int productID;
    ArrayList<Products> ProductList;
    Button EditButton, CancelButton, DeleteButton, UpdateButton;
    TextView ProductTitleAdmin, ProductDescriptionAdmin, ProductCategoryAdmin, ProductPriceAdmin;
    EditText EditProductTitle, EditProductDescription, EditProductPrice;
    Spinner EditProductCategory;
    ImageView ProductImageAdmin, EditProductImage;
    RelativeLayout EditImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_view);
        initializeComponents();
        initializeListeners();
        sqlLiteDB();
        fillData();
    }

    public void fillData() {
        ProductList = sqLiteHelper.getAllProductData(productID);
        ProductTitleAdmin.setText(ProductList.get(0).getProductTitle());
        ProductDescriptionAdmin.setText(ProductList.get(0).getProductDescription());
        ProductCategoryAdmin.setText(ProductList.get(0).getProductCategory());
        int productPrice = ProductList.get(0).getProductPrice();
        String priceText = getResources().getString(R.string.txtProductPricePrefix, productPrice);
        ProductPriceAdmin.setText(priceText);

        byte[] productImageByte = ProductList.get(0).getProductImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        ProductImageAdmin.setImageBitmap(imageBitmap);
    }

    private void initializeComponents() {
        ProductList = new ArrayList<>();
        ProductTitleAdmin = findViewById(R.id.txtMainProductTitle);
        ProductDescriptionAdmin = findViewById(R.id.txtMainProductDescription);
        ProductCategoryAdmin = findViewById(R.id.txtMainProductCategory);
        ProductPriceAdmin = findViewById(R.id.txtMainProductPrice);
        ProductImageAdmin = findViewById(R.id.imgMainProductImage);
        EditButton = findViewById(R.id.btnEditProductDetailsAdmin);
        CancelButton = findViewById(R.id.btnEditCancelProductDetailsAdmin);
        DeleteButton = findViewById(R.id.btnDeleteProductAdmin);
        UpdateButton = findViewById(R.id.btnUpdateProductDetailsAdmin);
        productID = Integer.parseInt(getIntent().getExtras().get("productId").toString());
        EditProductImage = findViewById(R.id.imgMainProductImageUpdate);
        EditImageLayout = findViewById(R.id.EditImageLayout);
        EditProductTitle = findViewById(R.id.txtMainProductTitleUpdate);
        EditProductCategory = findViewById(R.id.txtMainProductCategoryUpdate);
        EditProductDescription = findViewById(R.id.txtMainProductDescriptionUpdate);
        EditProductPrice = findViewById(R.id.txtMainProductPriceUpdate);
    }

    private void initializeListeners() {
        EditButton.setOnClickListener(this);
        CancelButton.setOnClickListener(this);
        DeleteButton.setOnClickListener(this);
        UpdateButton.setOnClickListener(this);
        EditImageLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEditProductDetailsAdmin:
                switchView();
                break;
            case R.id.btnEditCancelProductDetailsAdmin:
                switchView();
                break;
            case R.id.btnDeleteProductAdmin:
                deleteConfirmationDialog();
                break;
            case R.id.btnUpdateProductDetailsAdmin:
                updateProduct();
                break;
            case R.id.EditImageLayout:
                chooseImage();
                break;
        }
    }

    private void chooseImage() {
        ActivityCompat.requestPermissions(
                AdminProductView.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_GALLERY
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(AdminProductView.this, "У вас нет разрешения на доступ к галлереи", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            EditProductImage.setImageBitmap(AdminMenu.decodeBitmapFromFilePath(AdminMenu.getPath(uri, this), 200, displayMetrics.widthPixels));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void switchView() {
        fillData();
        EditProductTitle.setText(ProductList.get(0).getProductTitle());
        EditProductDescription.setText(ProductList.get(0).getProductDescription());
        EditProductPrice.setText(String.valueOf(ProductList.get(0).getProductPrice()));
        String CategoryName = ProductList.get(0).getProductCategory();
        for (int i = 0; i < EditProductCategory.getCount(); i++) {
            if (EditProductCategory.getItemAtPosition(i).equals(CategoryName)) {
                EditProductCategory.setSelection(i);
            }
        }
        byte[] productImageByte = ProductList.get(0).getProductImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        EditProductImage.setImageBitmap(imageBitmap);
        ViewSwitcher viewSwitcherImage = findViewById(R.id.ViewSwitcherImage);
        viewSwitcherImage.showNext();
        ViewSwitcher viewSwitcherTitle = findViewById(R.id.ViewSwitcherTitle);
        viewSwitcherTitle.showNext();
        ViewSwitcher viewSwitcherDescription = findViewById(R.id.ViewSwitcherDescription);
        viewSwitcherDescription.showNext();
        ViewSwitcher viewSwitcherCategory = findViewById(R.id.ViewSwitcherCategory);
        viewSwitcherCategory.showNext();
        ViewSwitcher viewSwitcherPrice = findViewById(R.id.ViewSwitcherPrice);
        viewSwitcherPrice.showNext();
        ViewSwitcher viewSwitcherButton = findViewById(R.id.ViewSwitcherButton);
        viewSwitcherButton.showNext();
    }

    private void deleteProduct() {
        try {
            sqLiteHelper.deleteData(productID);
            Toast.makeText(this, "Продукт удалён!", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Удаление не удачно!\nОшибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        finish();
    }

    private void deleteConfirmationDialog() {
        final AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(AdminProductView.this);
        confirmationDialog.setCancelable(false);
        confirmationDialog.setMessage("Вы уверены что хотите удалить этот продукт?");
        confirmationDialog.setTitle("Удалить продукт");
        confirmationDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProduct();
            }
        });
        confirmationDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        confirmationDialog.show();
    }

    private void updateProduct() {
        AdminMenu adminMenu = new AdminMenu();
        try {
            if (inputValidation()) {
                sqLiteHelper.updateData(
                        productID,
                        EditProductTitle.getText().toString(),
                        EditProductCategory.getSelectedItem().toString(),
                        Integer.parseInt(EditProductPrice.getText().toString()),
                        EditProductDescription.getText().toString(),
                        adminMenu.imageViewToByte(EditProductImage));
            }
            Toast.makeText(this, "Информация о продукте успешно обновлена!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Обновление с ошибкой!\nОшибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        switchView();
    }

    private boolean inputValidation() {
        boolean valid;
        Drawable drawable = EditProductImage.getDrawable();

        BitmapDrawable bitmapDrawable = drawable instanceof BitmapDrawable ? (BitmapDrawable)drawable : null;
        if (drawable == null || bitmapDrawable.getBitmap() == null || bitmapDrawable == null) {
            valid = false;
            Toast.makeText(this, "Пожалуйста, включите изображение продукта", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(EditProductTitle.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Пожалуйста, введите название продукта", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(EditProductDescription.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Пожалуйста, введите описание продукта", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(EditProductPrice.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Пожалуйста, введите цену продукта", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }

        return valid;
    }

    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(AdminProductView.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productTable (productId INTEGER PRIMARY KEY AUTOINCREMENT, productTitle VARCHAR, productCategory VARCHAR, productPrice INTEGER, productDescription VARCHAR, productImage BLOB)");
    }
}
