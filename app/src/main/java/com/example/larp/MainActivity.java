package com.example.larp;

import static android.view.View.generateViewId;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View;

import com.example.larp.DraggableTextView;
import com.example.larp.R;
import com.example.larp.VerticalTextView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import androidx.core.util.Pair;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.app.AlertDialog;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    // 添加位置記錄變數
    private float savedDateX = 50;
    private float savedDateY = 50;
    private float savedWeekdayX = 70;
    private float savedWeekdayY = 50;
    private float savedEventX = 50;
    private float savedEventY = 100;
    private float savedLocationX;
    private float savedLocationY;
    private float savedOrganizerX = 50;
    private float savedOrganizerY = 100;

    private TextInputEditText datePickerEditText;
    private TextInputEditText eventNameEditText;
    private TextInputEditText locationEditText;
    private TextInputEditText organizerEditText;
    private TextView weekdayText;
    private SwitchMaterial dateRangeSwitch;
    private MaterialDatePicker<?> currentDatePicker;
    private ImageView backgroundImage;
    private ShapeableImageView organizerLogo;
    private Slider dateSizeSlider;
    private Slider weekdaySizeSlider;
    private Slider eventSizeSlider;
    private Slider locationSizeSlider;
    private Slider organizerSizeSlider;

    private float dateTextSize = 72;
    private float weekdayTextSize = 36;
    private float eventTextSize = 36;
    private float locationTextSize = 80;
    private float organizerTextSize = 20;

    private static final String[] WEEKDAYS = {"日", "一", "二", "三", "四", "五", "六"};
    private String selectedOrganizer ="";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int MIN_TEXT_LENGTH = 1;
    private static final int MAX_TEXT_LENGTH = 50;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1002;
    private static final int MANAGE_STORAGE_PERMISSION_REQUEST_CODE = 1003;
    private static final int API_30 = 30; // Android 11

    private Spinner organizerSpinner;
    private ImageView logoView;
    private RelativeLayout textContainer;
    Typeface textFont1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 設定視窗調整模式，避免鍵盤擋住輸入區域
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        checkAndRequestPermissions();
        initializeViews();
        initializeDatePickers();
        initializeFontSizeSliders();
        setupButtons();
        textFont1 = Typeface.createFromAsset(getResources().getAssets(), "fonts/GenSekiGothic-B.ttc");


    }

    private void checkAndRequestPermissions() {
        // 檢查 Android 版本
        if (android.os.Build.VERSION.SDK_INT >= API_30) {
            // Android 11 及以上版本需要 MANAGE_EXTERNAL_STORAGE 權限
            if (!Environment.isExternalStorageManager()) {
                try {
                    // 嘗試使用 ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_REQUEST_CODE);
                } catch (Exception e) {
                    // 如果上面的方法不可用，嘗試使用 ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_REQUEST_CODE);
                }
            } else {
                Toast.makeText(this, "已獲得所有檔案存取權限", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Android 10 及以下版本需要 WRITE_EXTERNAL_STORAGE 權限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                Toast.makeText(this, "已獲得儲存權限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MANAGE_STORAGE_PERMISSION_REQUEST_CODE) {
            if (android.os.Build.VERSION.SDK_INT >= API_30) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "已獲得所有檔案存取權限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "需要檔案存取權限才能保存圖片", Toast.LENGTH_LONG).show();
                    // 顯示說明對話框
                    showPermissionExplanationDialog();
                }
            }
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("需要權限")
                .setMessage("此應用程式需要檔案存取權限才能保存圖片。請在設定中授予權限。")
                .setPositiveButton("前往設定", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已獲得儲存權限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要儲存權限才能保存圖片", Toast.LENGTH_LONG).show();
                // 可以再次請求權限或顯示說明
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showPermissionExplanationDialog();
                }
            }
        }
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= API_30) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "請先授予檔案存取權限", Toast.LENGTH_LONG).show();
                checkAndRequestPermissions();
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "請先授予儲存權限", Toast.LENGTH_LONG).show();
                checkAndRequestPermissions();
                return;
            }
        }
    }

    private void validateInputs() throws IllegalArgumentException {
        String eventName = eventNameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        if (eventName.length() < MIN_TEXT_LENGTH || eventName.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("活動名稱長度必須在 " + MIN_TEXT_LENGTH + " 到 " + MAX_TEXT_LENGTH + " 個字元之間");
        }

        if (location.length() < MIN_TEXT_LENGTH || location.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("地點長度必須在 " + MIN_TEXT_LENGTH + " 到 " + MAX_TEXT_LENGTH + " 個字元之間");
        }
    }

    private void saveImage() {
        try {
            validateInputs();

            // 檢查權限
            if (android.os.Build.VERSION.SDK_INT >= API_30) {
                if (!Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "請先授予檔案存取權限", Toast.LENGTH_LONG).show();
                    checkAndRequestPermissions();
                    return;
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "請先授予儲存權限", Toast.LENGTH_LONG).show();
                    checkAndRequestPermissions();
                    return;
                }
            }

            // 有權限，執行儲存操作
            saveImageUsingMediaStore();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "儲存圖片時發生錯誤: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveImageUsingMediaStore() {
        try {
            Bitmap bitmap = createBitmapFromView();
            if (bitmap == null) {
                throw new IllegalStateException("無法創建圖片");
            }

            String fileName = "LARP_Event_" + System.currentTimeMillis() + ".jpg";
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File outputFile = new File(picturesDir, fileName);

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Toast.makeText(this, "圖片已保存至: " + outputFile.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "儲存圖片時發生IO錯誤: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "儲存圖片時發生未知錯誤: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        datePickerEditText = findViewById(R.id.datePickerEditText);
        weekdayText = findViewById(R.id.weekdayText);
        dateRangeSwitch = findViewById(R.id.dateRangeSwitch);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        locationEditText = findViewById(R.id.locationEditText);
        backgroundImage = findViewById(R.id.backgroundImage);
        organizerLogo = findViewById(R.id.organizerLogo);
        textContainer = findViewById(R.id.textContainer);

        organizerSpinner = findViewById(R.id.organizerSpinner);

        // 設定開關監聽器
        dateRangeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            datePickerEditText.setText("");
            weekdayText.setText("");
            updateDatePicker(isChecked);
            updateTextDisplay();
        });

        // 設定文字輸入監聽器
        datePickerEditText.setOnClickListener(v ->
                currentDatePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

        // 為所有文字輸入欄位添加監聽器
        addTextWatcher(eventNameEditText);
        addTextWatcher(locationEditText);

        // 設定主辦單位下拉選單
        String[] organizers = {
            "請選擇舉辦活動的團體名",
            "中古世紀實境遊戲",
            "台灣實境角色扮演聯盟",
            "伊諾斯維",
            "紅藍爭霸",
            "假日古戰場",
            "異世界的史帝芬",
            "塔客文創交流協會",
            "新月旅團",
            "貓衣櫥",
            "蘭城異譜LARP同好會",
            "LARP BAER",
            "LARP TIMES",
            "LARP宗親會",
            "萊茵軍事休閒協會",
            "NTW新台灣娛樂摔角聯盟",
            "靠過來桌遊店"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            organizers
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        organizerSpinner.setAdapter(adapter);

        // 設定 Spinner 的選擇監聽器
        organizerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOrganizer = organizers[position];
                Log.d("MainActivity", "Selected organizer: " + selectedOrganizer);
                if (position > 0) { // 如果不是預設選項才更新 logo
                    updateOrganizerLogo(selectedOrganizer);
                    updateTextDisplay(); // 更新文字顯示
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("MainActivity", "No organizer selected");
            }
        });
    }

    private void addTextWatcher(TextInputEditText editText) {
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTextDisplay();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void initializeDatePickers() {
        updateDatePicker(false);
    }

    private void initializeFontSizeSliders() {
        dateSizeSlider = findViewById(R.id.dateSizeSlider);
        weekdaySizeSlider = findViewById(R.id.weekdaySizeSlider);
        eventSizeSlider = findViewById(R.id.eventSizeSlider);
        locationSizeSlider = findViewById(R.id.locationSizeSlider);
        organizerSizeSlider = findViewById(R.id.organizerSizeSlider);

        // 設定滑動監聽器
        addSliderListener(dateSizeSlider, value -> dateTextSize = value);
        addSliderListener(weekdaySizeSlider, value -> weekdayTextSize = value);
        addSliderListener(eventSizeSlider, value -> eventTextSize = value);
        addSliderListener(locationSizeSlider, value -> locationTextSize = value);
        addSliderListener(organizerSizeSlider, value -> organizerTextSize = value);
    }

    private void addSliderListener(Slider slider, OnSliderValueChangeListener listener) {
        slider.addOnChangeListener((s, value, fromUser) -> {
            if (fromUser) {
                listener.onValueChanged(value);
                updateTextDisplay();
            }
        });
    }

    private interface OnSliderValueChangeListener {
        void onValueChanged(float value);
    }

    private void updateDatePicker(boolean isRangeMode) {
        if (isRangeMode) {
            currentDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("選擇日期範圍")
                    .setSelection(
                            new Pair<>(
                                    MaterialDatePicker.todayInUtcMilliseconds(),
                                    MaterialDatePicker.todayInUtcMilliseconds()
                            )
                    )
                    .setCalendarConstraints(new CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointForward.now())
                            .build())
                    .build();

            ((MaterialDatePicker<Pair<Long, Long>>) currentDatePicker)
                    .addOnPositiveButtonClickListener(this::handleDateRangeSelection);
        } else {
            currentDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("選擇日期")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(new CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointForward.now())
                            .build())
                    .build();

            ((MaterialDatePicker<Long>) currentDatePicker)
                    .addOnPositiveButtonClickListener(this::handleSingleDateSelection);
        }
    }

    private void handleSingleDateSelection(Long selection) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);

        String dateStr = String.format(Locale.getDefault(), "%d",
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerEditText.setText(dateStr);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        weekdayText.setText("(" + WEEKDAYS[dayOfWeek] + ")");

        updateTextDisplay();
    }

    private void handleDateRangeSelection(Pair<Long, Long> selection) {
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(selection.first);

        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(selection.second);

        if (selection.first.equals(selection.second)) {
            String dateStr = String.format(Locale.getDefault(), "%d",
                    startDate.get(Calendar.DAY_OF_MONTH));
            datePickerEditText.setText(dateStr);

            int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK) - 1;
            weekdayText.setText("(" + WEEKDAYS[dayOfWeek] + ")");
        } else {
            String dateRangeStr = String.format(Locale.getDefault(), "%d~%d",
                    startDate.get(Calendar.DAY_OF_MONTH),
                    endDate.get(Calendar.DAY_OF_MONTH));
            datePickerEditText.setText(dateRangeStr);
            weekdayText.setText("");
        }

        updateTextDisplay();
    }

    private void setupButtons() {
        Button saveButton = findViewById(R.id.saveButton);
        Button clearButton = findViewById(R.id.clearButton);

        saveButton.setOnClickListener(v -> saveImage());
        clearButton.setOnClickListener(v -> clearAllInputs());
    }

    private void updateTextDisplay() {

        // 在清除之前保存現有元素的位置和大小
        for (int i = 0; i < textContainer.getChildCount(); i++) {
            android.view.View child = textContainer.getChildAt(i);
            if (child instanceof DraggableTextView) {
                DraggableTextView textView = (DraggableTextView) child;
                if (child.getTag() != null && textView.isPositionSet()) {
                    switch (child.getTag().toString()) {
                        case "date":
                            savedDateX = textView.getLastX();
                            savedDateY = textView.getLastY();
                            Log.i("*date" , "*date X: " + savedDateX + " Y: " + savedDateY);
                            break;
                        case "weekday":
                            savedWeekdayX = textView.getLastX();
                            savedWeekdayY = textView.getLastY();
                            Log.i("*weekday" , "*weekday X: " + savedWeekdayX + " Y: " + savedWeekdayY);
                            break;
                        case "event":
                            savedEventX = textView.getLastX();
                            savedEventY = textView.getLastY();
                            Log.i("*event" , "*event X: " + savedEventX + " Y: " + savedEventY);
                            break;
                        case "organizer":
                            savedOrganizerX = textView.getLastX();
                            savedOrganizerY = textView.getLastY();
                            Log.i("*organizer" , "*organizer X: " + savedOrganizerX + " Y: " + savedOrganizerY);
                            break;
                    }
                }
            } else if (child instanceof VerticalTextView) {
                savedLocationX = child.getX();
                savedLocationY = child.getY();
            }
        }
        // 保存 organizerLogo 的引用
        logoView = findViewById(R.id.organizerLogo);
        textContainer.removeView(logoView);

        textContainer.removeAllViews();

        // 重新添加 logo
        textContainer.addView(logoView);

        // 創建並添加日期文字
        DraggableTextView dateText = createDraggableTextView(
                datePickerEditText.getText().toString(),
                dateTextSize,
                Color.parseColor("#8F0F0F"),
                "date"
        );

        // 恢復位置
        if (savedDateX != 0 || savedDateY != 0) {
            dateText.setLastPosition(savedDateX, savedDateY);
        } else {
            // 設定預設位置
            dateText.setLastPosition(50, 50);
        }

        textContainer.addView(dateText);

        // 創建並添加星期文字
        if (!weekdayText.getText().toString().isEmpty()) {
            DraggableTextView weekdayTextView = createDraggableTextView(
                    weekdayText.getText().toString(),
                    weekdayTextSize,
                    Color.parseColor("#8F0F0F"),
                    "weekday"
            );

            // 恢復位置
            if (savedWeekdayX != 0 || savedWeekdayY != 0) {
                weekdayTextView.setLastPosition(savedWeekdayX, savedWeekdayY);
            } else {
                // 設定預設位置
                weekdayTextView.setLastPosition(70, 50);
            }

            textContainer.addView(weekdayTextView);
        }

        //創建並添加活動名稱文字
        DraggableTextView eventText = createDraggableTextView(
                eventNameEditText.getText().toString(),
                eventTextSize,
                Color.parseColor("#8F0F0F"),
                "event"
        );

        // 恢復位置
        if (savedEventX != 0 || savedEventY != 0) {
            eventText.setLastPosition(savedEventX, savedEventY);
        } else {
            // 設定預設位置
            eventText.setLastPosition(50, 100);
        }

        textContainer.addView(eventText);

        // 創建並添加地點文字
        if (!locationEditText.getText().toString().isEmpty()) {
            VerticalTextView locationText = createVerticalTextView(
                    locationEditText.getText().toString(),
                    locationTextSize,
                    Color.parseColor("#8F0F0F")
            );

            // 恢復位置
            if (savedLocationX != 0 || savedLocationY != 0) {
                locationText.setX(savedLocationX);
                locationText.setY(savedLocationY);
            }

            textContainer.addView(locationText);
        }

        // 創建並添加主辦單位文字
        DraggableTextView organizerText = createDraggableTextView(
                selectedOrganizer,
                organizerTextSize,
                Color.parseColor("#492207"),
                "organizer"
        );

        textContainer.addView(organizerText);
    }

    private DraggableTextView createDraggableTextView(String text, float textSize, int textColor, String tag) {
        DraggableTextView textView = new DraggableTextView(this);
        textView.setId(generateViewId());
        textView.setTag(tag);
        textView.setText(text);
        textView.setTextColor(textColor);
        textView.setTextSize(textSize);
        textView.setTypeface(textFont1);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        // 根據不同的標籤設定不同的預設位置
        if (tag.equals("organizer")) {
            params.leftMargin = logoView.getWidth()+ 32;   // logo 寬度 60dp + 邊距 16dp + 間距 14dp
            params.topMargin = textContainer.getHeight() - logoView.getHeight() - (logoView.getHeight()/3); // 底部邊距
        }
//        else if (tag.equals("event")) {
//            params.leftMargin = 100;
//            params.topMargin = textContainer.getHeight()/4;
//        }
//        else if (tag.equals("date")) {
//            params.leftMargin = 50;
//            params.topMargin = 50;
//        } else if (tag.equals("event")) {
//            params.leftMargin = 100;
//            params.topMargin = textContainer.getHeight()/4;
//        } else {
//            params.leftMargin = 50;
//            params.topMargin = 50;
//        }
        
        textView.setLayoutParams(params);
        return textView;
    }

    private VerticalTextView createVerticalTextView(String text, float textSize, int textColor) {
        VerticalTextView textView = new VerticalTextView(this);
        textView.setId(generateViewId());
        textView.setText(text);
        textView.setTextColor(textColor);
        textView.setTextSize(textSize);
        textView.setVerticalText(true);
        textView.setAlpha(0.4f);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTypeface(textFont1);
//        textView.setAlpha();

        // 設定內邊距
        textView.setPadding(20, 20, 20, 20);

        // 使用 RelativeLayout.LayoutParams
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        textView.setLayoutParams(params);

        // 設定初始位置
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // 如果沒有保存的位置，則使用預設位置
        if (savedLocationX == 0 && savedLocationY == 0) {
            savedLocationX = screenWidth - 600;
            savedLocationY = screenHeight / 12;
        }

        // 使用 setX/Y 來設置位置
        textView.setX(savedLocationX);
        textView.setY(savedLocationY);

        Log.d("MainActivity", "Created VerticalTextView: text=" + text +
              ", size=" + textSize + ", x=" + savedLocationX + ", y=" + savedLocationY);

        return textView;
    }

    private void clearAllInputs() {
        // 清除所有輸入
        datePickerEditText.setText("");
        eventNameEditText.setText("");
        locationEditText.setText("");
        organizerSpinner.setSelection(0);
        
        // 重置所有滑塊到預設值
        dateSizeSlider.setValue(72);
        weekdaySizeSlider.setValue(36);
        eventSizeSlider.setValue(36);
        locationSizeSlider.setValue(80);
        organizerSizeSlider.setValue(20);
        
        // 清除預覽區域
        textContainer.removeAllViews();
        
        // 重新添加 logo
        if (organizerLogo != null) {
            textContainer.addView(organizerLogo);
        }
        
        // 顯示提示訊息
        Toast.makeText(this, "已清除所有設定", Toast.LENGTH_SHORT).show();
    }

    private Bitmap createBitmapFromView() {
        FrameLayout view = findViewById(R.id.imageContainer);
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void updateOrganizerLogo(String organizer) {
        int logoResId;

        switch (organizer) {
            case "請選擇舉辦活動的團體名":
            case "中古世紀實境遊戲":
                logoResId = R.drawable.medieval_reality_game;
                break;
            case "台灣實境角色扮演聯盟":
                logoResId = R.drawable.taiwan_realistic_role_playing_alliance;
                break;
            case "伊諾斯維":
                logoResId = R.drawable.inosvi;
                break;
            case "紅藍爭霸":
                logoResId = R.drawable.red_and_blue_hegemony;
                break;
            case "假日古戰場":
                logoResId = R.drawable.holiday_ancient_battlefield;
                break;
            case "異世界的史帝芬":
                logoResId = R.drawable.stephen_from_another_world;
                break;
            case "塔客文創交流協會":
                logoResId = R.drawable.taco_cultural_and_creative_exchange_association;
                break;
            case "新月旅團":
                logoResId = R.drawable.crescent_tour_group;
                break;
            case "貓衣櫥":
                logoResId = R.drawable.cat_wardrobe;
                break;
            case "蘭城異譜LARP同好會":
                logoResId = R.drawable.lancheng_different_spectrum_larp_fan_club;
                break;
            case "LARP BAER":
                logoResId = R.drawable.larpbear;
                break;
            case "LARP TIMES":
                logoResId = R.drawable.larptime;
                break;
            case "LARP宗親會":
                logoResId = R.drawable.larp_clan_association;
                break;
            case "萊茵軍事休閒協會":
                logoResId = R.drawable.rmra;
                break;
            case "靠過來桌遊店":
                logoResId = R.drawable.grow_life;
                break;
            default:
                logoResId = R.drawable.default_logo;
                break;
        }

        organizerLogo.setImageResource(logoResId);
    }
}