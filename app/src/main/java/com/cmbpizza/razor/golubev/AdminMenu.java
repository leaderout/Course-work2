package com.cmbpizza.razor.golubev;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AdminMenu extends Activity implements View.OnClickListener, SearchView.OnQueryTextListener, SearchView.OnSuggestionListener, SearchView.OnCloseListener, ListView.OnItemClickListener  {

    private static final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_ICON_1
    };
    private static SQLLiteHelperProducts sqLiteHelper;
    final int REQUEST_CODE_GALLERY = 999;
    public int orderCount = 0;
    public Dialog addProductDialog;
    public Dialog orderDialog;
    FloatingActionButton FabAddProduct;
    Button AddProductButton, CancelButton, ChooseImageButton, CheckOrderButton;
    EditText AddProductTitle, AddProductPrice, AddProductDescription;
    Spinner SpinnerProductCategory;
    ImageView ImgProductImageView;
    SearchView AdminSearchView;
    ListView ProductListView, OrderListView;
    ArrayList<Products> ProductList;
    ArrayList<Integer> ProductIDList;
    ArrayList<Orders> OrderList;
    ProductListAdapter ListAdapter;
    OrderListAdapter OrderListAdapter;
    private SearchSuggestionsAdapter mSuggestionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
        initializeComponents();
        initializeListeners();
        sqlLiteDB();
        getAllData();
        getAllSearchSuggestions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AdminSearchView.setIconified(true);
        AdminSearchView.clearFocus();
        getOrderCount();
        checkOrderCount();
        getAllData();
    }

    private void initializeComponents() {
        orderDialog = new Dialog(AdminMenu.this);
        orderDialog.setContentView(R.layout.admin_check_order_dialog);

        ProductListView = findViewById(R.id.adminListView);
        AdminSearchView = findViewById(R.id.adminSearchView);
        OrderListView = orderDialog.findViewById(R.id.adminOrderListView);

        AutoCompleteTextView searchAutoCompleteTextView = AdminSearchView.findViewById(AdminSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        searchAutoCompleteTextView.setThreshold(0);
        ProductList = new ArrayList<>();
        ProductIDList = new ArrayList<>();
        OrderList = new ArrayList<>();
        ListAdapter = new ProductListAdapter(AdminMenu.this, R.layout.admin_product_items_list, ProductList);
        ProductListView.setAdapter(ListAdapter);
        OrderListAdapter = new OrderListAdapter(orderDialog.getContext(), R.layout.admin_check_order_list_view, OrderList);
        OrderListView.setAdapter(OrderListAdapter);


        FabAddProduct = findViewById(R.id.add_product_fab);
        CheckOrderButton = findViewById(R.id.adminOrderCheckButton);

        addProductDialog = new Dialog(AdminMenu.this);
        addProductDialog.setContentView(R.layout.admin_add_product_dialog);

        AddProductButton = addProductDialog.findViewById(R.id.btnAdminAddProduct);
        CancelButton = addProductDialog.findViewById(R.id.btnAdminCancel);
        ChooseImageButton = addProductDialog.findViewById(R.id.btnAdminAddProductImage);
        ImgProductImageView = addProductDialog.findViewById(R.id.imgProductImage);
        AddProductTitle = addProductDialog.findViewById(R.id.txtNewProductTitle);
        AddProductPrice = addProductDialog.findViewById(R.id.txtNewProductPrice);
        AddProductDescription = addProductDialog.findViewById(R.id.txtNewProductDescription);
        SpinnerProductCategory = addProductDialog.findViewById(R.id.spinnerAddProductCategory);
    }

    private void initializeListeners() {
        AddProductButton.setOnClickListener(this);
        CancelButton.setOnClickListener(this);
        ChooseImageButton.setOnClickListener(this);
        FabAddProduct.setOnClickListener(this);
        AdminSearchView.setOnQueryTextListener(this);
        AdminSearchView.setOnSuggestionListener(this);
        AdminSearchView.setOnCloseListener(this);
        ProductListView.setOnItemClickListener(this);
        CheckOrderButton.setOnClickListener(this);

        OrderListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                getOrderCount();
                checkOrderCount();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAdminAddProduct:
                addProductToDB();
                break;
            case R.id.btnAdminCancel:
                AddProductTitle.getText().clear();
                AddProductPrice.getText().clear();
                AddProductDescription.getText().clear();
                ImgProductImageView.setImageBitmap(null);
                addProductDialog.cancel();
                getAllData();
                break;
            case R.id.btnAdminAddProductImage:
                ActivityCompat.requestPermissions(
                        AdminMenu.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
                break;
            case R.id.add_product_fab:
                addProductFAB();
                break;
            case R.id.adminOrderCheckButton:
                showOrderDialog();
                break;
        }
    }

    private void showOrderDialog() {
        getOrderDetails();
        orderDialog.setCancelable(true);
        orderDialog.setCanceledOnTouchOutside(true);
        orderDialog.show();
    }

    private void getOrderDetails() {
        OrderList.clear();
        //Toast.makeText(this, "ArrayLength: " + String.valueOf(sqLiteHelper.getCartCount(randomID)), Toast.LENGTH_SHORT).show();
        OrderList.addAll(sqLiteHelper.getAllOrderItems());
        //Toast.makeText(this, "ArrayLength: " + String.valueOf(CartItemsList.size()), Toast.LENGTH_SHORT).show();
        OrderListAdapter.notifyDataSetChanged();
    }

    private void getOrderCount() {
        orderCount = sqLiteHelper.getOrderCount();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView ListProductId = view.findViewById(R.id.productId);
        Intent newIntent = new Intent(AdminMenu.this, AdminProductView.class);

        Pair<View, String> pairImage = Pair.create(view.findViewById(R.id.productImage), "productImageAdmin");
        Pair<View, String> pairTitle = Pair.create(view.findViewById(R.id.productTitle), "productTitleAdmin");
        Pair<View, String> pairDescription = Pair.create(view.findViewById(R.id.productDescription), "productDescriptionAdmin");
        Pair<View, String> pairCategory = Pair.create(view.findViewById(R.id.productCategory), "productCategoryAdmin");
        Pair<View, String> pairPrice = Pair.create(view.findViewById(R.id.productPrice), "productPriceAdmin");

        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                AdminMenu.this,
                pairImage,
                pairTitle,
                pairDescription,
                pairCategory,
                pairPrice);
        newIntent.putExtra("productId", ListProductId.getText().toString());

        startActivity(newIntent, activityOptions.toBundle());
    }

    public static String getPath(Uri uri, Context context) {
        if (uri == null) {
            Toast.makeText(context, "Невозможно обновить картинку, поробуйте ещё раз", Toast.LENGTH_SHORT).show();
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    public static Bitmap decodeBitmapFromFilePath(String path, int reqHeight, int reqWidth) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqHeight, reqWidth);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqHeight, int reqWidth) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void getAllSearchSuggestions() {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        for(int i: ProductIDList){
            cursor.addRow(new String[]{String.valueOf(sqLiteHelper.getProductID(i)), sqLiteHelper.getProductTitle(i), android.util.Base64.encodeToString(sqLiteHelper.getProductImage(i), Base64.DEFAULT)});
        }
        mSuggestionsAdapter = new SearchSuggestionsAdapter(AdminSearchView.getContext(), cursor, R.layout.search_suggestion_view, false);
        AdminSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
    }

    public void getSearchTextSuggestions(String searchString) {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        ArrayList<Products> filterProductList = new ArrayList<>();
        filterProductList.clear();
        if (!searchString.isEmpty()) {
            String searchStringLower = searchString.toLowerCase();
            for (Products list : ProductList) {
                String ProductName = list.getProductTitle().toLowerCase();
                if (ProductName.startsWith(searchStringLower)) {
                    filterProductList.add(list);
                }
            }
            for(Products filterList: filterProductList){
                cursor.addRow(new String[]{String.valueOf(filterList.getProductId()), filterList.getProductTitle(), android.util.Base64.encodeToString(filterList.getProductImage(), Base64.DEFAULT)});
            }
        } else {
            for(int i: ProductIDList){
                cursor.addRow(new String[]{String.valueOf(sqLiteHelper.getProductID(i)), sqLiteHelper.getProductTitle(i), android.util.Base64.encodeToString(sqLiteHelper.getProductImage(i), Base64.DEFAULT)});
            }
        }
        mSuggestionsAdapter = new SearchSuggestionsAdapter(AdminSearchView.getContext(), cursor, R.layout.search_suggestion_view, false);
        AdminSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
    }


    public byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(AdminMenu.this, "У вас нет разрешения для доступа к галлереи", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            ImgProductImageView.setImageBitmap(decodeBitmapFromFilePath(getPath(uri, this), 150, 150));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addProductFAB() {
        addProductDialog.setCancelable(true);
        addProductDialog.setCanceledOnTouchOutside(true);
        addProductDialog.show();
    }

    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(AdminMenu.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productTable (productId INTEGER PRIMARY KEY AUTOINCREMENT, productTitle VARCHAR, productCategory VARCHAR, productPrice INTEGER, productDescription VARCHAR, productImage BLOB)");
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productOrderTable (orderId INTEGER PRIMARY KEY, memberId INTEGER, idList VARCHAR, quantityList VARCHAR, totalPrice INTEGER, orderStatus INTEGER)");
    }

    private void getAllData() {
        ProductIDList.clear();
        ProductList.clear();
        for (Products product: sqLiteHelper.getAllData()) {
            ProductIDList.add(product.getProductId());
            ProductList.add(new Products(product.getProductId(), product.getProductTitle(), product.getProductCategory(), product.getProductPrice(), product.getProductDescription(), product.getProductImage()));
        }
        ListAdapter.notifyDataSetChanged();
    }

    private void addProductToDB() {
        if (inputValidation()) {
            try {
                sqLiteHelper.insertData(
                        AddProductTitle.getText().toString().trim(),
                        SpinnerProductCategory.getSelectedItem().toString(),
                        Integer.parseInt(AddProductPrice.getText().toString()),
                        AddProductDescription.getText().toString(),
                        imageViewToByte(ImgProductImageView)
                );
                Toast.makeText(AdminMenu.this, "Продукт успешно добавлен!", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            getAllData();

            AddProductTitle.getText().clear();
            AddProductPrice.getText().clear();
            AddProductDescription.getText().clear();
            ImgProductImageView.setImageBitmap(null);
            addProductDialog.cancel();
        }
    }

    private boolean inputValidation() {
        boolean valid;
        if (ImgProductImageView.getDrawable() == null) {
            valid = false;
            Toast.makeText(this, "Пожалуйста, включите изображение продукта", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(AddProductTitle.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Пожалуйста, введите название продукта", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(AddProductPrice.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Пожалуйста, введите цену продукта", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(AddProductDescription.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Пожалуйста, введите описание продукта", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }

        return valid;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        String searchStringLower = s.toLowerCase();
        ArrayList<Products> filterProductList = new ArrayList<>();
        for (Products list : ProductList) {
            String ProductName = list.getProductTitle().toLowerCase();
            if (ProductName.startsWith(searchStringLower)) {
                filterProductList.add(list);
            }
        }
        ProductList.clear();
        ProductList.addAll(filterProductList);
        ListAdapter.notifyDataSetChanged();
        filterProductList.clear();
        Toast.makeText(this, "Вы искали: " + s, Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String searchString) {
        if (!searchString.isEmpty()) {
            getSearchTextSuggestions(searchString);
        } else {
            getAllSearchSuggestions();
            getAllData();
        }
        return true;
    }

    @Override
    public boolean onSuggestionSelect(int i) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int i) {
        Cursor c = (Cursor) mSuggestionsAdapter.getItem(i);
        String query = c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
        AdminSearchView.setQuery(query, true);
        return true;
    }

    @Override
    public boolean onClose() {
        getAllData();
        return true;
    }

    private void checkOrderCount() {
        RelativeLayout relativeLayoutButton, relativeLayoutBadge;
        relativeLayoutButton = findViewById(R.id.layoutRelativeOrderCheckButton);
        relativeLayoutBadge = findViewById(R.id.layoutRelativeOrderButtonBadge);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        if (orderCount > 0) {
            layoutParams.setMargins(layoutParams.getMarginStart(), 0, layoutParams.getMarginEnd(), 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.layout.activity_admin_menu);
            relativeLayoutButton.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams layoutParamsBadge = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsBadge.setMargins(0, 0, 0, 0);
            layoutParamsBadge.addRule(RelativeLayout.ALIGN_START, R.id.layoutLinearOrderCheckButton);
            layoutParamsBadge.addRule(RelativeLayout.ALIGN_TOP, R.id.layoutLinearOrderCheckButton);
            relativeLayoutBadge.setLayoutParams(layoutParamsBadge);
            relativeLayoutBadge.setVisibility(View.VISIBLE);
            TextView cartBadgeCount = findViewById(R.id.txtOrderButtonBadge);
            cartBadgeCount.setText(String.valueOf(orderCount));
        } else {
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -50.0f, getResources().getDisplayMetrics());
            layoutParams.setMargins(layoutParams.getMarginStart(), 0, layoutParams.getMarginEnd(), value);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.layout.activity_admin_menu);
            relativeLayoutButton.setLayoutParams(layoutParams);
        }
    }
}
