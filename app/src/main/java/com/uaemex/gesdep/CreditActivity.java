package com.uaemex.gesdep;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.Timestamp;
import com.uaemex.gesdep.models.UserModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// IMPORTACIÓN FALTANTE
import com.uaemex.gesdep.utils.WindowUtils;


public class CreditActivity extends AppCompatActivity {

    private TextView tvCurrentBalance;
    private EditText etRechargeAmount;
    private MaterialButton btnSimulateRecharge;
    private RecyclerView rvTransactions;
    private TextView tvEmptyHistory;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TransactionAdapter adapter;
    private List<TransactionModel> transactionList = new ArrayList<>();
    private UserModel currentUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        WindowUtils.setGreenStatusBar(this); // Ahora debe funcionar
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadUserDataAndBalance();
        loadTransactionHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDataAndBalance();
        loadTransactionHistory();
    }

    private void initViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        etRechargeAmount = findViewById(R.id.etRechargeAmount);
        btnSimulateRecharge = findViewById(R.id.btnSimulateRecharge);
        rvTransactions = findViewById(R.id.rvTransactions);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        btnSimulateRecharge.setOnClickListener(v -> simulateRecharge());
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void loadUserDataAndBalance() {
        if (auth.getCurrentUser() == null) return;

        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    // LECTURA SEGURA DE CRÉDITO
                    if (doc.exists()) {
                        Double credit = doc.getDouble("appCredit");
                        updateBalanceUI(credit != null ? credit : 0.0);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar saldo", Toast.LENGTH_SHORT).show());
    }

    private void updateBalanceUI(double balance) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        tvCurrentBalance.setText(format.format(balance));
    }

    private void loadTransactionHistory() {
        if (auth.getCurrentUser() == null) return;

        db.collection("transactions")
                .whereEqualTo("userId", auth.getCurrentUser().getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    transactionList.clear();
                    if (!querySnapshot.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                            transactionList.add(mapSnapshotToTransactionModel(doc));
                        }
                        tvEmptyHistory.setVisibility(View.GONE);
                    } else {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private TransactionModel mapSnapshotToTransactionModel(DocumentSnapshot doc) {
        TransactionModel model = new TransactionModel();
        model.id = doc.getId();
        model.userId = doc.getString("userId");
        model.description = doc.getString("description");

        // Manejo seguro de la fecha
        Object dateObject = doc.get("createdAt");
        if (dateObject instanceof Long) {
            model.createdAt = new Date((Long) dateObject);
        } else if (dateObject instanceof Timestamp) {
            model.createdAt = ((Timestamp) dateObject).toDate();
        } else if (dateObject instanceof Date) {
            model.createdAt = (Date) dateObject;
        } else {
            model.createdAt = new Date(0); // Fecha por defecto
        }

        // Mapeo seguro de double
        Double amount = doc.getDouble("amount");
        model.amount = amount != null ? amount : 0.0;

        model.type = doc.getString("type");

        return model;
    }

    private void simulateRecharge() {
        String amountStr = etRechargeAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            etRechargeAmount.setError("Ingrese un monto");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etRechargeAmount.setError("El monto debe ser positivo");
                return;
            }

            DocumentReference userRef = db.collection("users").document(auth.getCurrentUser().getUid());

            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot userSnapshot = transaction.get(userRef);
                double currentCredit = userSnapshot.getDouble("appCredit") != null ? userSnapshot.getDouble("appCredit") : 0.0;

                double newCredit = currentCredit + amount;
                transaction.update(userRef, "appCredit", newCredit);

                // 2. Registrar transacción
                DocumentReference transRef = db.collection("transactions").document();
                TransactionModel trans = new TransactionModel(
                        transRef.getId(),
                        auth.getCurrentUser().getUid(),
                        "Recarga de Saldo",
                        amount,
                        "RECHARGE",
                        new Date()
                );
                transaction.set(transRef, trans);

                return null;
            }).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Recarga de $" + amount + " exitosa.", Toast.LENGTH_LONG).show();
                etRechargeAmount.setText("");
                loadUserDataAndBalance(); // Recargar saldo
                loadTransactionHistory(); // Recargar historial
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error en la recarga: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } catch (NumberFormatException e) {
            etRechargeAmount.setError("Monto inválido");
        }
    }

    // --- MODELO DE TRANSACCIÓN (Mínimo requerido) ---
    public static class TransactionModel {
        public String id;
        public String userId;
        public String description;
        public double amount;
        public String type;
        public Date createdAt;

        public TransactionModel() {}
        public TransactionModel(String id, String userId, String description, double amount, String type, Date createdAt) {
            this.id = id;
            this.userId = userId;
            this.description = description;
            this.amount = amount;
            this.type = type;
            this.createdAt = createdAt;
        }

        public String getId() { return id; }
        public String getUserId() { return userId; }
        public String getDescription() { return description; }
        public double getAmount() { return amount; }
        public String getType() { return type; }
        public Date getCreatedAt() { return createdAt; }
    }

    // --- ADAPTADOR DE TRANSACCIONES ---
    class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private List<TransactionModel> list;
        private NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", new Locale("es", "MX"));

        public TransactionAdapter(List<TransactionModel> list) { this.list = list; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(v);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TransactionModel item = list.get(position);
            holder.tvDescription.setText(item.description);

            if (item.createdAt != null) {
                holder.tvDate.setText(sdf.format(item.createdAt));
            } else {
                holder.tvDate.setText("Fecha desconocida");
            }

            String sign;
            int color;

            if (item.type != null && item.type.equals("RECHARGE")) {
                sign = "+";
                color = Color.parseColor("#4CAF50"); // Verde (Ganancia/Recarga)
            } else if (item.amount > 0) {
                sign = "-";
                color = Color.RED; // Rojo (Gasto)
            } else {
                sign = "";
                color = Color.GRAY;
            }

            holder.tvAmount.setTextColor(color);
            holder.tvAmount.setText(sign + format.format(Math.abs(item.amount)));

            holder.ivIcon.setImageResource(item.type != null && item.type.equals("RECHARGE") ? R.drawable.ic_attach_money : R.drawable.ic_check);
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDescription, tvDate, tvAmount;
            ImageView ivIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }
}