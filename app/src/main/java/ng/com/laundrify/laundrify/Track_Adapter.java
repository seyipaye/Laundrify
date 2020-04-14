package ng.com.laundrify.laundrify;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static ng.com.laundrify.laundrify.TrackActivity.customerHistoryDB;
import static ng.com.laundrify.laundrify.TrackActivity.customerOrderDB;
import static ng.com.laundrify.laundrify.TrackActivity.orderDB;
import static ng.com.laundrify.laundrify.TrackActivity.uWCDC;
import static ng.com.laundrify.laundrify.TrackActivity.uWCDay;
import static ng.com.laundrify.laundrify.TrackActivity.uWCHour;
import static ng.com.laundrify.laundrify.TrackActivity.uWCMins;

public class Track_Adapter extends RecyclerView.Adapter<Track_Adapter.MyViewHolder> {
    private List<Track_Model> track_models;
    Context context;
    RecyclerView.LayoutManager layoutManager;
    static final int CALL_REQUEST = 100;
    double todayDouble;

    //todo remove listener from button after click


    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView id, status, contact, coTime, coAdd, deTime, deAdd, itemDrop, dcCompanyName, deliveryMan, dcNumber;
        RecyclerView items;
        Button button, button2, button3, callBtn;
        RatingBar ratingBar;
        LinearLayout ratingLayout;

        //View Holder
        public MyViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.orderDp);
            this.button2 = itemView.findViewById(R.id.button2);
            this.button3 = itemView.findViewById(R.id.button3);
            this.callBtn = itemView.findViewById(R.id.callButton);
            this.id = itemView.findViewById(R.id.orderID);
            this.status = itemView.findViewById(R.id.orderStatus);
            this.contact = itemView.findViewById(R.id.ordeContact);
            this.coTime = itemView.findViewById(R.id.orderCoTime);
            this.coAdd = itemView.findViewById(R.id.orderCoAdd);
            this.deTime = itemView.findViewById(R.id.orderDeTime);
            this.deAdd = itemView.findViewById(R.id.orderDeAdd);
            this.items = itemView.findViewById(R.id.orderItems);
            this.itemDrop = itemView.findViewById(R.id.itemDrop);
            this.button = itemView.findViewById(R.id.orderButton);
            this.ratingBar = itemView.findViewById(R.id.ratingBar);
            this.dcCompanyName = itemView.findViewById(R.id.dcCompanyName);
            this.deliveryMan = itemView.findViewById(R.id.deliveryMan);
            this.dcNumber = itemView.findViewById(R.id.dcNumber);
            this.ratingLayout = itemView.findViewById(R.id.ratingLayout);
            //context = itemView.getContext();
        }
    }

    public Track_Adapter(List<Track_Model> data, Context context) {
        this.track_models = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.track_row, viewGroup, false);
        //view.setOnClickListener(PickupActivity.myOnClickListener);

        SimpleDateFormat sdfStamp = new SimpleDateFormat("yyyyMMdd");
        todayDouble = chgD(sdfStamp.format(new Date()));

        Track_Adapter.MyViewHolder myViewHolder = new Track_Adapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ImageView imageView = holder.imageView;
        final int listPosition = holder.getAdapterPosition();
        Button button2 = holder.button2;
        Button button3 = holder.button3;
        Button callBtn = holder.callBtn;
        TextView id = holder.id;
        TextView status = holder.status;
        final TextView contact = holder.contact;
        TextView coTime = holder.coTime;
        final TextView coAdd = holder.coAdd;
        TextView deTime = holder.deTime;
        TextView deAdd = holder.deAdd;
        final Button button = holder.button;
        final RatingBar ratingBar = holder.ratingBar;
        final TextView itemDrop = holder.itemDrop;
        final RecyclerView items = holder.items;
        TextView dcCompanyName = holder.dcCompanyName;
        TextView deliveryMan = holder.deliveryMan;
        TextView dcNumber = holder.dcNumber;
        LinearLayout ratingLayout = holder.ratingLayout;

        final String orderID = track_models.get(listPosition).getId();

        Log.i("Stuff", "Button is " + track_models.get(listPosition).getButton() + "Id is " + orderID);
        Log.i("test", "Bind: " + listPosition);

        //Reset flexible things to default
        button2.setVisibility(View.GONE);
        button3.setVisibility(View.GONE);
        ratingLayout.setVisibility(View.GONE);
        button3.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        //Dc Details
        dcCompanyName.setText("Company name: " + track_models.get(listPosition).getDcCompanyName());
        deliveryMan.setText(": " + track_models.get(listPosition).getDeliveryManName());
        deliveryMan.setText("Delivery Man name: " + track_models.get(listPosition).getDeliveryManName());
        dcNumber.setText("Phone number: " + track_models.get(listPosition).getdCPhoneNumber());
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callPhoneNumber(track_models.get(listPosition).getdCPhoneNumber());
            }
        });

        //Pending
        if (track_models.get(listPosition).getButton() == 0) {
            button.setText("Loading...");
            status.setText("Status: " + "Pending");
            button2.setVisibility(View.VISIBLE);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProgressDialog("Please wait...", "Canceling order " + context.getString(R.string.confusedFace), (long) 15000);
                    orderDB.child(orderID).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            hideProgressDialog();
                            context.startActivity(new Intent(context, MainActivity.class));
                        }
                    });
                }
            });

            //About to collect
        } else if (track_models.get(listPosition).getButton() == 1) {
            button.setText("Confirm Collection");
            status.setText("Status: " + "On our way");
            ratingLayout.setVisibility(View.VISIBLE);

            // If order is today or in the past
            double orderDay = chgD(track_models.get(position).getCollectionStamp().substring(0, 8));
            if (todayDouble >= orderDay) {

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ratingBar.getRating() > 0) {
                            if (ratingBar.getRating() <= 3) {
                                askForRemark(context.getString(R.string.confusedFace) + " What went wrong ?", false);
                            } else {
                                askForRemark(context.getString(R.string.grinningFaceWithSmilingEyes) + " How good was our service?", true);
                            }
                        } else {
                            Toast.makeText(context, "FAILED: Please rate the collection first", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //Ask for remark
                    private void askForRemark(String title, final boolean excellent) {
                        LayoutInflater inflater = LayoutInflater.from(context);
                        View dView = inflater.inflate(R.layout.edittext_dialog, null);
                        final EditText dEditText = dView.findViewById(R.id.dialogEditText);
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                new ContextThemeWrapper(context, R.style.AlertDialogCustom));

                        // Set Positive button
                        builder.setTitle(title)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        changeLevel(orderID, listPosition, 2);
                                        lodgeRating(ratingBar.getRating(), dEditText.getText().toString(), orderID, track_models.get(listPosition).getDcKey(), "Collections");
                                        Toast.makeText(context, "Thank you", Toast.LENGTH_LONG).show();
                                    }
                                });

                        // Set negative button
                        if (excellent) {
                            builder.setNegativeButton("Not today", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    changeLevel(orderID, listPosition, 2);
                                    lodgeRating(ratingBar.getRating(), dEditText.getText().toString(), orderID, track_models.get(listPosition).getDcKey(), "Collections");
                                    Toast.makeText(context, "Thank you", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Toast.makeText(context, "Confirmation cancelled", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        builder.setView(dView);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            } else {

                // If order is in future
                button.setText("Cancel");
                status.setText("Status: " + "To be collected");
                ratingLayout.setVisibility(View.GONE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Remove order
                        showProgressDialog("Please wait...", "Canceling order " + context.getString(R.string.confusedFace), (long) 15000);
                        orderDB.child(orderID).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                hideProgressDialog();
                                context.startActivity(new Intent(context, MainActivity.class));
                            }
                        });
                    }
                });
            }

            //In progress
        } else if (track_models.get(listPosition).getButton() == 2) {
            if (track_models.get(listPosition).getPrice() != 0 && !track_models.get(listPosition).isPaid()) {
                button.setText("Pay ₦" + track_models.get(listPosition).getPrice());
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_credit_card_black_24dp, 0);

                status.setText("Status: " + "In Progress");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(context, "You clicked button ! " + listPosition, Toast.LENGTH_SHORT).show();
                        payMoney(listPosition);
                    }
                });
            } else if (track_models.get(listPosition).isPaid() || track_models.get(listPosition).getPrice() == 0) {
                button.setText("Please wait...");
                status.setText("Status: " + "In Progress");
                button.setClickable(false);
            }

            // Ready for delivery
        } else if (track_models.get(listPosition).getButton() == 3) {
            //Log.i("stuff", "Equals 3");

            status.setText("Status: " + "Delivering");

            if (track_models.get(listPosition).getPrice() != 0 && !track_models.get(listPosition).isPaid()) {

                //If not paid show option
                button3.setVisibility(View.VISIBLE);
                button3.setText("Pay ₦" + track_models.get(listPosition).getPrice());
                button3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_credit_card_black_24dp, 0);
                button3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(context, "You clicked button ! " + listPosition, Toast.LENGTH_SHORT).show();
                        payMoney(listPosition);
                    }
                });
            }

            // If paid or not confirm Delivery
            if (track_models.get(listPosition).isWeeklyPickup()) {
                button.setText("Confirm Exchange");
            } else {
                button.setText("Confirm Delivery");
            }

            ratingLayout.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ratingBar.getRating() > 0) {
                        if (ratingBar.getRating() <= 3) {
                            askForRemark(context.getString(R.string.confusedFace) + " What went wrong ?", false);
                        } else {
                            askForRemark(context.getString(R.string.grinningFaceWithSmilingEyes) + " We'll like to hear your experience ?", true);
                        }
                    } else {
                        Toast.makeText(context, "FAILED: Please rate delivery first", Toast.LENGTH_LONG).show();
                    }
                }

                private void askForRemark(String title, boolean excellent) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View dView = inflater.inflate(R.layout.edittext_dialog, null);
                    final EditText dEditText = dView.findViewById(R.id.dialogEditText);
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            new ContextThemeWrapper(context, R.style.AlertDialogCustom));

                    // Set positive button
                    builder.setTitle(title)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    lodgeRating(ratingBar.getRating(), dEditText.getText().toString(), orderID, track_models.get(listPosition).getDcKey(), "Deliveries");
                                    confirmDelivery(orderID, listPosition, track_models.get(listPosition).isPaid());
                                    Toast.makeText(context, "Thank you", Toast.LENGTH_LONG).show();
                                }
                            });

                    // Set negative button
                    if (excellent) {
                        builder.setNegativeButton("Not today", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                lodgeRating(ratingBar.getRating(), dEditText.getText().toString(), orderID, track_models.get(listPosition).getDcKey(), "Deliveries");
                                confirmDelivery(orderID, listPosition, track_models.get(listPosition).isPaid());
                                Toast.makeText(context, "Thank you", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Toast.makeText(context, "Confirmation Cancelled", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    builder.setView(dView);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            //Confirming Payment
        } else if (track_models.get(listPosition).getButton() == 4) {
            button.setText("Confirming Payment...");
            status.setText("Status: " + "Delivered");
        }

        //Items list
        if (!track_models.get(listPosition).getItems().isEmpty()) {
            itemDrop.setVisibility(View.VISIBLE);
            itemDrop.setOnClickListener(new View.OnClickListener() {
                boolean closed = true;

                @Override
                public void onClick(View v) {
                    if (closed) {
                        layoutManager = new LinearLayoutManager(context);
                        items.setLayoutManager(layoutManager);
                        items.setVisibility(View.VISIBLE);
                        items.setAdapter(new Items_Adapter(track_models.get(listPosition).getItems()));
                        items.setNestedScrollingEnabled(false);
                        closed = false;
                        itemDrop.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
                    } else {
                        itemDrop.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
                        items.setVisibility(View.GONE);
                        closed = true;
                    }
                }
            });
        }

        Glide.with(holder.itemView)
                .load(track_models.get(listPosition).getDp())
                .into(imageView);
        id.setText("ID: " + orderID);
        contact.setText(track_models.get(listPosition).getContact());
        coTime.setText("Time: " + track_models.get(listPosition).getCoTime());
        coAdd.setText("Address: " + track_models.get(listPosition).getDeAdd());
        deTime.setText("Time: " + track_models.get(listPosition).getDeTime());
        deAdd.setText("Address: " + track_models.get(listPosition).getDeAdd());
    }

    private void payMoney(int listPosition) {
        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra("Price", track_models.get(listPosition).getPrice());
        intent.putExtra("FirstName", TrackActivity.fname);
        intent.putExtra("LastName", TrackActivity.lname);
        intent.putExtra("IsWeeklyPickup", track_models.get(listPosition).isWeeklyPickup());
        intent.putExtra("PublicKey", TrackActivity.publicKey);
        intent.putExtra("EncryptionKey", TrackActivity.encryptionKey);
        intent.putExtra("UserEmail", TrackActivity.uEmail);
        intent.putExtra("Agent", track_models.get(listPosition).getDcCompanyName());
        intent.putExtra("OrderID", track_models.get(listPosition).getId());
        intent.putExtra("DCKey", track_models.get(listPosition).getDcKey());
        intent.putExtra("SecretKey", TrackActivity.secretKey);
        intent.putExtra("OrderProgress", track_models.get(listPosition).getButton());
        context.startActivity(intent);
    }

    private void lodgeRating(float rating, String comment, String orderID, String dcID, String path) {
        final String ratingS = String.valueOf(rating);
        if (!comment.matches("")) {
            TrackActivity.database.getReference().child("Users").child("DryCleaners").child(dcID).child("Profile")
                    .child("Comments").child(orderID).setValue(comment);
        }

        if (ratingS.contains(".")) {
            TrackActivity.database.getReference().child("Users").child("DryCleaners").child(dcID).child("Ratings")
                    .child(path).child(orderID).setValue(ratingS.replace(".", "d"));
        } else {
            TrackActivity.database.getReference().child("Users").child("DryCleaners").child(dcID).child("Ratings")
                    .child("Collections").child(orderID).setValue(ratingS);
        }

    }

    private void changeLevel(String orderID, final int position, final int nextLevel) {
        showProgressDialog("", "Loading...", (long) 60000);
        final DatabaseReference orderBase = orderDB.child(orderID);
        orderBase.child("Progress").setValue(nextLevel, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                hideProgressDialog();
                track_models.get(position).setButton(nextLevel);
                TrackActivity.recyclerAdapter.notifyItemChanged(position);
            }
        });
    }

    private void confirmDelivery(final String orderID, final int position, final boolean isPaid) {
        showProgressDialog("Please wait", "Communicating with our servers...", (long) 60000);

        // Get Order Snapshot
        orderDB.child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                final Map orderMap = (Map) dataSnapshot.getValue();

                // Check if not paid for history
                if (!isPaid){
                    orderMap.put("PaymentMethod", "Cash");
                }

                //Add it to Main history
                DatabaseReference historyDB = TrackActivity.database.getReference().child("History").child("Orders");
                historyDB.child(orderID).setValue(orderMap,
                        new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                // Add to Customer's DB
                                customerHistoryDB.child("Orders").child(orderID).child("TimeStamp")
                                        .setValue(track_models.get(position).getDeliveryStamp(), new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                // Add to DC's DB
                                                if (!track_models.get(position).getDcKey().matches("")) {
                                                    TrackActivity.database.getReference().child("Users").child("DryCleaners").child(track_models.get(position).getDcKey())
                                                            .child("History").child("Orders").child(orderID).child("TimeStamp")
                                                            .setValue(track_models.get(position).getDeliveryStamp(), new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                                    // Check if it's a weeklyPickup to recycle
                                                                    if (track_models.get(position).isWeeklyPickup()) {
                                                                        recycleOrder(position, orderMap, orderID);
                                                                    } else {

                                                                        if (isPaid) {

                                                                            // Remove if paid
                                                                            orderDB.child(orderID).removeValue();
                                                                        } else {

                                                                            // Inform if comfirming  payment
                                                                            orderDB.child(orderID).child("Progress").setValue(4);
                                                                        }

                                                                        // Clear UI
                                                                        hideProgressDialog();
                                                                        try {
                                                                            track_models.remove(position);
                                                                            TrackActivity.recyclerAdapter.notifyItemRemoved(position);
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                            TrackActivity.recyclerAdapter.notifyDataSetChanged();
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    hideProgressDialog();
                                                    Toast.makeText(context, "Something went wrong, try again later", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            }
                                        });
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void recycleOrder(final int position, final Map orderInfo, final String lastOrderID) {

        // Check if DC is not canceled & if DC dosen't handle weekly pickup
        if (uWCDC != null && !uWCDC.matches(orderInfo.get("DryCleanerKey").toString())) {
            if (track_models.get(position).isPaid()) {
                // Remove if paid
                orderDB.child(lastOrderID).removeValue();
            } else {
                // Inform if confirming  payment
                orderDB.child(lastOrderID).child("Progress").setValue(4);
            }

            // Clear UI
            try {
                track_models.remove(position);
                TrackActivity.recyclerAdapter.notifyItemRemoved(position);
            } catch (Exception e) {
                e.printStackTrace();
                TrackActivity.recyclerAdapter.notifyDataSetChanged();
            }
            hideProgressDialog();
            return;
        }

        //Make calender
        Calendar reCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMM, yyyy | hh:mm aa.");
        SimpleDateFormat sdfStamp = new SimpleDateFormat("yyyyMMddHHmmss");


        boolean foundDay = false;

        // Get next day
        do {
            int dayOfWeek = reCal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == uWCDay) {
                foundDay = true;
            } else {
                reCal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } while (!foundDay);

        reCal.set(Calendar.HOUR_OF_DAY, uWCHour);
        reCal.set(Calendar.MINUTE, uWCMins);

        Log.i("test", "New Stuffs is " + reCal.getTime());

        final String preId = orderDB.push().getKey();
        final String orderID = "WP-" + preId.substring(1, 5) + preId.substring(16);

        // Get Collection details
        final Date coDate = reCal.getTime();
        String coTime = sdf.format(coDate);
        final String coStamp = sdfStamp.format(coDate);

        // Get Delivery details
        reCal.add(Calendar.DAY_OF_YEAR, 7);
        final Date deDate = reCal.getTime();
        String deTime = sdf.format(deDate);
        final String deStamp = sdfStamp.format(deDate);

        // Put details
        orderInfo.put("CollectionTime", coTime);
        orderInfo.put("CollectionStamp", coStamp);
        orderInfo.put("DeliveryTime", deTime);
        orderInfo.put("DeliveryStamp", deStamp);
        orderInfo.put("Progress", 2);
        orderInfo.put("Price", 0);
        orderInfo.put("HavePaid", false);

        Log.i("test", "New Recycled Pickup is " + orderInfo);

        // Upload to main
        orderDB.child(orderID).setValue(orderInfo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                // Upload to customer DB
                customerOrderDB.child(orderID).child("TimeStamp").setValue(coStamp, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        // Upload to DC's Collected
                        TrackActivity.database.getReference().child("Users").child("DryCleaners").child(orderInfo.get("DryCleanerKey").toString())
                                .child("Orders").child("Collected").child(coStamp.substring(0, 8)).child(orderID).child("TimeStamp")
                                .setValue(coStamp, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                // Upload to DC's Delivery
                                TrackActivity.database.getReference().child("Users").child("DryCleaners").child(orderInfo.get("DryCleanerKey").toString())
                                        .child("Orders").child("Delivery").child(deStamp.substring(0, 8)).child(orderID).child("TimeStamp")
                                        .setValue(deStamp, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        // Remove if paid
                                        if (track_models.get(position).isPaid()) {
                                            //Remove if paid
                                            orderDB.child(lastOrderID).removeValue();
                                        } else {
                                            // Inform if confirming  payment
                                            orderDB.child(lastOrderID).child("Progress").setValue(4);
                                        }
                                        hideProgressDialog();
                                        try {
                                            track_models.remove(position);
                                            TrackActivity.recyclerAdapter.notifyItemRemoved(position);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            TrackActivity.recyclerAdapter.notifyDataSetChanged();
                                        }

                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void callPhoneNumber(String finalToNumber) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST);
                    return;
                }
            }
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + finalToNumber));
            context.startActivity(callIntent);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private double chgD(Object obj) {
        if (obj != null) {
            return Double.valueOf(String.valueOf(obj));
        } else {
            return 0;
        }
    }

    //Progress dialog
    Handler handler = new Handler();
    public ProgressDialog mProgressDialog;
    public void showProgressDialog(String title, String message, Long time) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (title.matches("")) {
            mProgressDialog = new ProgressDialog(context, R.style.AlertDialogCustom);
        } else {
            mProgressDialog = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
            mProgressDialog.setTitle(title);
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(mProgressDialog!=null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    Toast.makeText(context, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, time);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public int getItemCount() {
        return track_models.size();
    }
}
