package com.cmbpizza.razor.golubev;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.app.Activity;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class UserMenu extends Activity implements View.OnClickListener, SearchView.OnSuggestionListener, SearchView.OnCloseListener, AdapterView.OnItemClickListener {
    private static final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_ICON_1
    };
    public String memberId;
    public static boolean isUserLoggedIn = false, sort = true;
    private static SQLLiteHelperProducts sqLiteHelper;
    public int cartCount = 0;
    public String randomID;
    public Dialog checkoutDialog;
    FloatingActionButton FilterFab, FilterCheapFab, FilterExpensiveFab, ClearCartFab;
    TextView CartNetTotal;
    Button CheckoutButton, ConfirmCheckoutButton;
    SearchView UserSearchView;
    ViewStub ListViewStub;
    ListView ProductListView, CheckoutItemsListView;
    ArrayList<CartItems> CartItemsList;
    ArrayList<Products> ProductList;
    ArrayList<Integer> ProductIDList;
    CheckoutListAdapter CheckoutItemsListAdapter;
    ProductListAdapter ListAdapter;
    private SearchSuggestionsAdapter mSuggestionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_menu);

        initializeComponents();
        initializeViewModes();
        initializeListeners();
        checkLoginStatus();
        checkView();
        generateRandomID();
        sqlLiteDB();
        getAllData();
        getCartCounter();
        checkCartCount();
        clearCart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCartCounter();
        checkLoginStatus();
        checkCartCount();
        getAllData();
    }

    private void getCartCounter() {
        cartCount = sqLiteHelper.getCartCount(randomID);
    }

    private void checkLoginStatus() {
        if(getIntent().getExtras() == null){
            isUserLoggedIn = false;
        } else {
            isUserLoggedIn = true;
            memberId = getIntent().getExtras().get("userId").toString();
        }
    }

    private void initializeComponents() {
        ListViewStub = findViewById(R.id.userListViewStub);

        checkoutDialog = new Dialog(UserMenu.this);
        checkoutDialog.setContentView(R.layout.user_checkout_dialog);
        CartNetTotal = checkoutDialog.findViewById(R.id.txtNetTotal);

        CheckoutItemsListView = checkoutDialog.findViewById(R.id.userCheckoutList);
        CartItemsList = new ArrayList<>();
        CheckoutItemsListAdapter = new CheckoutListAdapter(checkoutDialog.getContext(), R.layout.user_checkout_product_list_view, CartItemsList);
        CheckoutItemsListView.setAdapter(CheckoutItemsListAdapter);

        ConfirmCheckoutButton = checkoutDialog.findViewById(R.id.btnConfirmCheckout);

        FilterFab = findViewById(R.id.filter_fab);
        FilterCheapFab = findViewById(R.id.filter_cheap_fab);
        FilterExpensiveFab = findViewById(R.id.filter_expensive_fab);
        ClearCartFab = findViewById(R.id.clear_cart_fab);
        CheckoutButton = findViewById(R.id.userCheckoutButton);

        ProductList = new ArrayList<>();
        ProductIDList = new ArrayList<>();
        ListAdapter = new ProductListAdapter(UserMenu.this, R.layout.user_product_items_list_view, ProductList);
    }

    private void initializeListeners() {
        FilterFab.setOnClickListener(this);
        FilterCheapFab.setOnClickListener(this);
        FilterExpensiveFab.setOnClickListener(this);
        ProductListView.setOnItemClickListener(this);

        CheckoutButton.setOnClickListener(this);
        ConfirmCheckoutButton.setOnClickListener(this);
        ClearCartFab.setOnClickListener(this);

        ProductListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                this.currentScrollState = scrollState;
                if(currentScrollState == SCROLL_STATE_FLING){
                    UserSearchView.setAlpha(0.0f);
                } else if(currentScrollState == SCROLL_STATE_TOUCH_SCROLL){
                    UserSearchView.setAlpha(0.5f);
                } else if(currentScrollState == SCROLL_STATE_IDLE){
                    UserSearchView.setAlpha(1);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;
            }
        });

        CheckoutItemsListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                getCheckoutNetTotal();
                getCartCounter();
                checkCartCount();
            }
        });
    }

    private void initializeViewModes() {
        ProductListView = (ListView) ListViewStub.inflate();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.filter_fab:
                sortProductList();
                break;
            case R.id.clear_cart_fab:
                clearCart();
                checkCartCount();
                break;
            case R.id.userCheckoutButton:
                checkoutDialog();
                break;
            case R.id.btnConfirmCheckout:
                createOrder();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView ListProductId = view.findViewById(R.id.productId);
        Intent newIntent = new Intent(UserMenu.this, UserProductView.class);

        Pair<View, String> pairImage = Pair.create(view.findViewById(R.id.productImage), "productImage");
        Pair<View, String> pairTitle = Pair.create(view.findViewById(R.id.productTitle), "productTitle");
        Pair<View, String> pairDescription = Pair.create(view.findViewById(R.id.productDescription), "productDescription");
        Pair<View, String> pairCategory = Pair.create(view.findViewById(R.id.productCategory), "productCategory");
        Pair<View, String> pairPrice = Pair.create(view.findViewById(R.id.productPrice), "productPrice");

        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                UserMenu.this,
                pairImage,
                pairTitle,
                pairDescription,
                pairCategory,
                pairPrice);
        newIntent.putExtra("userId", memberId);
        newIntent.putExtra("productId", ListProductId.getText().toString());
        newIntent.putExtra("cartId", randomID);

        startActivity(newIntent, activityOptions.toBundle());

    }

    private void checkView() {
        ListViewStub.setVisibility(View.VISIBLE);
        ProductListView.setAdapter(ListAdapter);
    }

    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(UserMenu.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productTable (productId INTEGER PRIMARY KEY AUTOINCREMENT, productTitle VARCHAR, productCategory VARCHAR, productPrice INTEGER, productDescription VARCHAR, productImage BLOB)");
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productCartTable (id INTEGER PRIMARY KEY AUTOINCREMENT, cartId VARCHAR, productId INTEGER, productQuantity INTEGER)");
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productOrderTable (orderId INTEGER PRIMARY KEY, memberId INTEGER, idList VARCHAR, quantityList VARCHAR, totalPrice INTEGER, orderStatus INTEGER)");
    }

    private void clearCart() {
        sqLiteHelper.dataQuery("DELETE FROM productCartTable");
        sqLiteHelper.dataQuery("DELETE FROM sqlite_sequence WHERE name='productCartTable'");
        cartCount = 0;
        Log.d("sql", "cart cleared");
    }

    private void getAllData() {
        ProductIDList.clear();
        ProductList.clear();
        for (Products product : sqLiteHelper.getAllData()) {
            ProductIDList.add(product.getProductId());
            ProductList.add(new Products(product.getProductId(), product.getProductTitle(), product.getProductCategory(), product.getProductPrice(), product.getProductDescription(), product.getProductImage()));
        }
        ListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onClose() {
        getAllData();
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
        UserSearchView.setQuery(query, true);
        return true;
    }

    private void checkCartCount() {
        RelativeLayout relativeLayoutButton, relativeLayoutBadge;
        relativeLayoutButton = findViewById(R.id.layoutRelativeCheckoutButton);
        relativeLayoutBadge = findViewById(R.id.layoutRelativeButtonBadge);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (cartCount > 0) {
            ClearCartFab.setVisibility(View.VISIBLE);
            ClearCartFab.setClickable(true);
            layoutParams.setMargins(layoutParams.getMarginStart(), 0, layoutParams.getMarginEnd(), 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.layout.activity_user_menu);
            relativeLayoutButton.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams layoutParamsBadge = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsBadge.setMargins(0, 0, 0, 0);
            layoutParamsBadge.addRule(RelativeLayout.ALIGN_START, R.id.layoutLinearCheckoutButton);
            layoutParamsBadge.addRule(RelativeLayout.ALIGN_TOP, R.id.layoutLinearCheckoutButton);
            relativeLayoutBadge.setLayoutParams(layoutParamsBadge);//the layout params is used to set the margin and the alignment
            relativeLayoutBadge.setVisibility(View.VISIBLE);

        } else {
            ClearCartFab.setVisibility(View.GONE);
            ClearCartFab.setClickable(false);

            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -60.0f, getResources().getDisplayMetrics());
            layoutParams.setMargins(layoutParams.getMarginStart(), 0, layoutParams.getMarginEnd(), value);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.layout.activity_user_menu);
            relativeLayoutButton.setLayoutParams(layoutParams);
        }
    }

    private void generateRandomID() {
        String randomNumbers = null;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i <= 10; i++) {
            Random random = new Random();
            stringBuilder.append(random.nextInt(9));
            randomNumbers = stringBuilder.toString();
        }
        randomID = randomNumbers;
    }

    @Override
    public void onBackPressed() {
        sqLiteHelper.dataQuery("DELETE FROM productCartTable");
        cartCount = 0;
        finish();
    }

    private void checkoutDialog() {
        getCheckoutDetails();
        checkoutDialog.setCancelable(true);
        checkoutDialog.setCanceledOnTouchOutside(true);
        checkoutDialog.show();
    }

    private void getCheckoutDetails() {
        CartItemsList.clear();
        int productNetTotalPrice = 0;

        for (int i = 1; i <= sqLiteHelper.getCartCount(randomID); i++) {
            String cartId = randomID;
            int productId = sqLiteHelper.getCartProductId(i);
            int productQuantity = sqLiteHelper.getCartProductQuantity(i);
            int productPrice = sqLiteHelper.getProductPrice(productId);
            productNetTotalPrice += (productPrice * productQuantity);
            CartItemsList.add(new CartItems(cartId, productId, productQuantity));
        }

        CartNetTotal.setText(getResources().getString(R.string.txtNetTotalPricePrefix, productNetTotalPrice));
        CheckoutItemsListAdapter.notifyDataSetChanged();
    }

    private void getCheckoutNetTotal(){
        int productNetTotalPrice = 0;
        for (int i = 1; i <= sqLiteHelper.getCartCount(randomID); i++) {
            int productId = sqLiteHelper.getCartProductId(i);
            int productQuantity = sqLiteHelper.getCartProductQuantity(i);
            int productPrice = sqLiteHelper.getProductPrice(productId);
            productNetTotalPrice += (productPrice * productQuantity);
        }

        CartNetTotal.setText(getResources().getString(R.string.txtNetTotalPricePrefix, productNetTotalPrice));
    }

    private void createOrder(){
        if(cartCount >= 1){
            int arrayLength = CartItemsList.size();
            String[] productIdList = new String[arrayLength];
            String[] productQuantityList = new String[arrayLength];
            int productNetTotalPrice = 0;

            for(int i = 0; i < arrayLength; i++){
                int productId = CartItemsList.get(i).getProductId();
                int productQuantity = CartItemsList.get(i).getProductQuantity();
                int productPrice = sqLiteHelper.getProductPrice(productId);

                productIdList[i] = String.valueOf(productId);
                productQuantityList[i] = String.valueOf(productQuantity);
                productNetTotalPrice += (productPrice * productQuantity);
            }
            sqLiteHelper.createOrder(randomID, memberId, productIdList, productQuantityList, productNetTotalPrice, 0);
            Toast.makeText(this, "Заказ номер: " + randomID + " отправлено на обработку!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
        }
        checkoutDialog.cancel();
    }

    private void sortProductList(){
        Collections.sort(ProductList);
        if(sort){
            Collections.reverse(ProductList);
            sort = !sort;
        }else{
            sort = !sort;
        }
        ListAdapter.notifyDataSetChanged();
    }
}
