package com.uaemex.gesdep;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.utils.WindowUtils;

public class RechargeActivity extends AppCompatActivity {

    private TextInputEditText etRechargeAmount;
    private TextView tvCurrentBalance, tvEmptyHistory;
    private MaterialButton btnSimulateRecharge;
    private RecyclerView rvTransactions;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private double currentBalance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);
        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        loadCurrentBalance();
        setupButtons();
    }

    private void initializeViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        etRechargeAmount = findViewById(R.id.etRechargeAmount);
        btnSimulateRecharge = findViewById(R.id.btnSimulateRecharge);
        rvTransactions = findViewById(R.id.rvTransactions);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
    }

    private void loadCurrentBalance() {
        if (auth.getCurrentUser() != null) {
            db.collection("users").document(auth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double balance = documentSnapshot.getDouble("balance");
                            currentBalance = balance != null ? balance : 0.0;
                            tvCurrentBalance.setText(String.format("$%.2f MXN", currentBalance));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar saldo", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupButtons() {
        btnSimulateRecharge.setOnClickListener(v -> processRecharge());
    }

    private void processRecharge() {
        String amountStr = etRechargeAmount.getText() != null ? etRechargeAmount.getText().toString().trim() : "";

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un monto", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simulación del proceso de recarga
            double newBalance = currentBalance + amount;

            if (auth.getCurrentUser() != null) {
                db.collection("users").document(auth.getCurrentUser().getUid())
                        .update("balance", newBalance)
                        .addOnSuccessListener(aVoid -> {
                            currentBalance = newBalance;
                            tvCurrentBalance.setText(String.format("$%.2f MXN", currentBalance));
                            etRechargeAmount.setText("");
                            Toast.makeText(this, String.format("Recarga exitosa: $%.2f", amount),
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al procesar recarga", Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
