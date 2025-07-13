package com.example.apd;

import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResultPageActivity extends AppCompatActivity {

    TextView dailyConsumption, monthlyConsumption, estimatedBill;
    PieChart pieChart;
    HashMap<String, Double> appliances = new HashMap<>();
    ArrayList<String> ratings = new ArrayList<>();
    double totalMonthly = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_page);

        CoordinatorLayout rootLayout = findViewById(R.id.rootLayout);
        dailyConsumption = findViewById(R.id.dailyConsumption);
        monthlyConsumption = findViewById(R.id.monthlyConsumption);
        estimatedBill = findViewById(R.id.estimatedBill);
        pieChart = findViewById(R.id.pieChart);
        ImageView backArrow = findViewById(R.id.backButton);
        Button downloadBtn = findViewById(R.id.downloadPdfButton);

        rootLayout.setAlpha(0f);
        rootLayout.animate().alpha(1f).setDuration(800).setStartDelay(100).start();
        pieChart.setScaleX(0f);
        pieChart.setScaleY(0f);
        pieChart.animate().scaleX(1f).scaleY(1f).setDuration(800).setStartDelay(200).start();

        backArrow.setOnClickListener(view -> {
            view.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                finish();
            }).start();
        });

        Intent intent = getIntent();
        appliances = (HashMap<String, Double>) intent.getSerializableExtra("map1");
        ratings = (ArrayList<String>) intent.getSerializableExtra("rating");
        totalMonthly = intent.getDoubleExtra("total", 0.0);
        String category = intent.getStringExtra("category");

        double ratePerUnit;
        try {
            String urlStr = "http://192.168.239.113:8080/arnab_backend_api/charges?category=" + category;
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));

            String response = br.readLine();
            if (response == null || response.trim().isEmpty()) {
                throw new Exception("No response from backend");
            }

            ratePerUnit = Double.parseDouble(response.trim());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to fetch rate per unit", Toast.LENGTH_SHORT).show();
            return;
        }

        showResults(appliances, totalMonthly, ratePerUnit);
        setupPieChart(appliances);

        downloadBtn.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            String daily = dailyConsumption.getText().toString().replaceAll(".*: ", "").replace(" kWh", "");
            String monthlyVal = monthlyConsumption.getText().toString().replaceAll(".*: ", "").replace(" kWh", "");
            String bill = estimatedBill.getText().toString().replaceAll(".*: ₹?", "");
            generatePdfFromMap(daily, monthlyVal, bill, appliances);
        });
    }

    private void showResults(HashMap<String, Double> appliances, double totalMonthly, double ratePerUnit) {
        LinearLayout applianceDetailsContainer = findViewById(R.id.applianceDetailsContainer);
        applianceDetailsContainer.removeAllViews();

        double totalDaily = totalMonthly / 30.0;
        double estimated = totalMonthly * ratePerUnit;

        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(true);
        tableLayout.setPadding(8, 8, 8, 8);
        tableLayout.setBackgroundColor(Color.WHITE);

        TableRow headerRow = new TableRow(this);
        addCellToRow(headerRow, "Appliance", true);
        addCellToRow(headerRow, "Rating", true);
        addCellToRow(headerRow, "Daily (kWh)", true);
        addCellToRow(headerRow, "Monthly (kWh)", true);
        tableLayout.addView(headerRow);

        int[] colors = ColorTemplate.MATERIAL_COLORS;
        int i = 0;

        for (Map.Entry<String, Double> entry : appliances.entrySet()) {
            String applianceName = entry.getKey();
            double monthly = entry.getValue();
            double daily = monthly / 30.0;
            String rawRating = (i < ratings.size()) ? ratings.get(i) : "N/A";
            String rating = rawRating.replace("Star", "★").trim();

            TableRow row = new TableRow(this);
            row.setBackgroundColor(Color.argb(25,
                    Color.red(colors[i % colors.length]),
                    Color.green(colors[i % colors.length]),
                    Color.blue(colors[i % colors.length])));

            addCellToRow(row, applianceName, false);
            addCellToRow(row, rating +" ★", false);
            addCellToRow(row, String.format("%.2f", daily), false);
            addCellToRow(row, String.format("%.2f", monthly), false);

            tableLayout.addView(row);
            i++;
        }

        applianceDetailsContainer.addView(tableLayout);

        dailyConsumption.setText(makeBold("Daily Total Energy Consumption: ", String.format("%.2f kWh", totalDaily)));
        monthlyConsumption.setText(makeBold("Monthly Total Energy Consumption: ", String.format("%.2f kWh", totalMonthly)));
        estimatedBill.setText(makeBold("Estimated Bill Amount: ₹", String.format("%.2f", estimated)));
    }

    private void addCellToRow(TableRow row, String text, boolean isHeader) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setTextSize(isHeader ? 16f : 14f);
        cell.setPadding(16, 16, 16, 16);
        cell.setTextColor(Color.BLACK);
        cell.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
        cell.setGravity(android.view.Gravity.CENTER);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        cell.setLayoutParams(params);
        row.addView(cell);
    }

    private SpannableStringBuilder makeBold(String label, String value) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(label);
        builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(value);
        return builder;
    }

    // setupPieChart(), generatePdfFromMap(), and drawHeaderFooter() are same as previously provided


    // Existing setupPieChart(), generatePdfFromMap(), drawHeaderFooter() remain unchanged
    private void setupPieChart(HashMap<String, Double> appliances) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(52f);
        pieChart.setTransparentCircleRadius(57f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText("Usage\nBreakdown");
        pieChart.setCenterTextSize(15f);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setDrawEntryLabels(false);
        pieChart.setEntryLabelTextSize(13f);
        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.animateX(1000, Easing.EaseInOutQuad);


        // Sort appliances by value descending
        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(appliances.entrySet());
        sortedList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<PieEntry> entries = new ArrayList<>();
        double othersTotal = 0;
        int count = 0;

        for (Map.Entry<String, Double> entry : sortedList) {
            if (count < 5) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            } else {
                othersTotal += entry.getValue();
            }
            count++;
        }

        if (othersTotal > 0) {
            entries.add(new PieEntry((float) othersTotal, "Others"));
        }

        // Distinct and accessible colors (visually distinguishable)
        List<Integer> colors = Arrays.asList(
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#FFC107"), // Amber
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#90A4AE")  // Grey for Others
        );

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // Refresh

        // Legend settings
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setWordWrapEnabled(true);
    }


    private void generatePdfFromMap(String daily, String monthly, String bill, HashMap<String, Double> appliances) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int pageWidth = 595, pageHeight = 842;
        int marginLeft = 40;
        int yPosition;
        int pageNumber = 1;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy • hh:mm a", Locale.getDefault());
        String dateTime = dateFormat.format(new Date());

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        drawHeaderFooter(canvas, pageWidth, pageHeight, dateTime);

        titlePaint.setTextSize(18f);
        titlePaint.setColor(Color.parseColor("#180166"));
        titlePaint.setFakeBoldText(true);
        canvas.drawText("Energy Consumption Report & Estimated Bill", marginLeft, 120, titlePaint);

        // Draw pie chart
        Bitmap chartBitmap = pieChart.getChartBitmap();
        if (chartBitmap != null) {
            Bitmap scaledChart = Bitmap.createScaledBitmap(chartBitmap, 300, 240, true);
            canvas.drawBitmap(scaledChart, marginLeft, 140, paint);
        }

        // Summary texts
        yPosition = 460;
        paint.setTextSize(15f);
        paint.setColor(Color.BLACK);
        canvas.drawText("🔋 Daily Total Energy Consumption: " + daily + " kWh", marginLeft, yPosition, paint);
        canvas.drawText("📅 Monthly Total Energy Consumption: " + monthly + " kWh", marginLeft, yPosition + 30, paint);
        canvas.drawText("💰 Estimated Bill Amount: ₹" + bill, marginLeft, yPosition + 60, paint);

        // Appliance table
        yPosition += 100;
        canvas.drawText("⚙ Appliance Details:", marginLeft, yPosition, paint);
        yPosition += 30;

        // Column headers
        headerPaint.setTextSize(15f);
        headerPaint.setFakeBoldText(true);
        headerPaint.setColor(Color.BLACK);
        canvas.drawText("Appliance", marginLeft, yPosition, headerPaint);
        canvas.drawText("Rating", marginLeft + 180, yPosition, headerPaint);
        canvas.drawText("Daily (kWh)", marginLeft + 280, yPosition, headerPaint);
        canvas.drawText("Monthly (kWh)", marginLeft + 420, yPosition, headerPaint);
        yPosition += 10;
        canvas.drawLine(marginLeft, yPosition, pageWidth - marginLeft, yPosition, paint);
        yPosition += 20;

        int[] colors = ColorTemplate.MATERIAL_COLORS;
        int index = 0;

        for (Map.Entry<String, Double> entry : appliances.entrySet()) {
            if (yPosition > pageHeight - 100) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                drawHeaderFooter(canvas, pageWidth, pageHeight, dateTime);
                yPosition = 100;
            }

            String appliance = entry.getKey();
            double monthlyVal = entry.getValue();
            double dailyVal = monthlyVal / 30.0;
            String rating = (index < ratings.size()) ? ratings.get(index).replace("Star", "★").trim() : "N/A";

            // Colored row background
            Paint bgPaint = new Paint();
            int bgColor = Color.argb(30,
                    Color.red(colors[index % colors.length]),
                    Color.green(colors[index % colors.length]),
                    Color.blue(colors[index % colors.length]));
            bgPaint.setColor(bgColor);
            canvas.drawRect(marginLeft - 10, yPosition - 15, pageWidth - marginLeft + 10, yPosition + 12, bgPaint);

            paint.setTextSize(14f);
            paint.setColor(Color.BLACK);
            canvas.drawText(appliance, marginLeft, yPosition, paint);
            canvas.drawText(rating, marginLeft + 180, yPosition, paint);
            canvas.drawText(String.format("%.2f", dailyVal), marginLeft + 280, yPosition, paint);
            canvas.drawText(String.format("%.2f", monthlyVal), marginLeft + 420, yPosition, paint);

            yPosition += 24;
            index++;
        }

        pdfDocument.finishPage(page);

        try {
            File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "EnergyReports");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            File pdfFile = new File(pdfDir, "Energy_Report_" + System.currentTimeMillis() + ".pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            Toast.makeText(this, "PDF saved to Downloads/EnergyReports", Toast.LENGTH_SHORT).show();

            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
        }
    }


    private void drawHeaderFooter(Canvas canvas, int pageWidth, int pageHeight, String dateTime) {
        Paint linePaint = new Paint();
        linePaint.setColor(Color.DKGRAY);
        linePaint.setStrokeWidth(2f);

        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(15f);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.RED);

        int centerX = pageWidth / 2;

        try {
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.apdcl_logo);
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logoBitmap, 40, 40, true);
            canvas.drawBitmap(scaledLogo, 40, 30, null);
        } catch (Exception ignored) {}

        canvas.drawLine(90, 50, centerX - 150, 50, linePaint);
        canvas.drawLine(centerX + 152, 50, pageWidth - 40, 50, linePaint);
        canvas.drawText("Assam Power Distribution Company Limited ", centerX, 55, textPaint);

        int footerY = pageHeight - 60;
        canvas.drawLine(160, footerY - 15, 435, footerY - 15, linePaint);

        try {
            Bitmap appLogo = BitmapFactory.decodeResource(getResources(), R.drawable.apdcl_logo);
            Bitmap scaledLogo = Bitmap.createScaledBitmap(appLogo, 40, 40, true);
            canvas.drawBitmap(scaledLogo, centerX - 20, footerY - 60, null);
        } catch (Exception ignored) {}

        Paint footerTextPaint = new Paint();
        footerTextPaint.setColor(Color.RED);
        footerTextPaint.setTextSize(15f);
        footerTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Generated by APDCL Calculator", centerX, footerY, footerTextPaint);

        Paint datePaint = new Paint();
        datePaint.setTextSize(14f);
        datePaint.setColor(Color.DKGRAY);
        datePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(dateTime, centerX, footerY + 20, datePaint);
    }
}
