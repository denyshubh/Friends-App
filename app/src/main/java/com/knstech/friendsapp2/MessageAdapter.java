package com.knstech.friendsapp2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import static android.support.constraint.Constraints.TAG;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    File BigImage,videoFile,docFile,audioFile;

    private Context context;

    public MessageAdapter(Context context, List<Messages> mMessageList) {
        this.context = context;
        this.mMessageList = mMessageList;
    }

    private View view;

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        this.view=view;
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        holder.messageText.setText("");

        imageStorageDir = new File(Environment.getExternalStorageDirectory()+"/Friends_App"+"/IMAGE");
        audioStorageDir =new File(Environment.getExternalStorageDirectory()+"/Friends_App"+"/AUDIO");
        videoStorageDir = new File(Environment.getExternalStorageDirectory()+"/Friends_App"+"/VIDEO");
        docStorageDir = new File(Environment.getExternalStorageDirectory()+"/Friends_App"+"/DOC");

        mAuth = FirebaseAuth.getInstance();
        final String current_uid = mAuth.getCurrentUser().getUid();
        final Messages c = mMessageList.get(position);
        final String from_user = c.getFrom();
        Object time = c.getTime();


        String timeStamp = time.toString();

        GetTimeAgo getTimeAgo = new GetTimeAgo();

        long lastTime = Long.parseLong(timeStamp);

        String messageTime = getTimeAgo.getTimeAgo(lastTime, null);
        holder.lastSeen.setText(messageTime);
        firebaseFileName = getName(c.getMessage());

        final String message_type = c.getType();
        boolean seen = c.isSeen();

        if (message_type.equals("text")) {

            LinearLayout msgWrapper = holder.textMsgWrapper;
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics());


            if (!from_user.equals(current_uid)) {
                msgWrapper.setGravity(Gravity.LEFT);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msgWrapper.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.setMargins(0, 0, margin, 0);
                msgWrapper.setLayoutParams(params);
                holder.messageText.setBackgroundResource(R.drawable.bg_msg_from);
                holder.messageTextExtended.setBackgroundResource(R.drawable.triangle_msg_from);


            } else {
                msgWrapper.setGravity(Gravity.RIGHT);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msgWrapper.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.setMargins(margin, 0, 0, 0);
                msgWrapper.setLayoutParams(params);
                holder.messageText.setBackgroundResource(R.drawable.bg_msg_you);
                holder.messageTextExtended.setBackgroundResource(R.drawable.triangle_msg_you);

            }


            holder.messageText.setText(c.getMessage());

          //  holder.messageImage.setEnabled(false);
        //   holder.messageImage.setVisibility(View.INVISIBLE);

        } else if(message_type.equalsIgnoreCase("image")){

              holder.messageText.setBackgroundResource(R.drawable.ic_image_black_24dp);
              File f= new File(imageStorageDir, "JPEG_"+firebaseFileName);

              this.BigImage =f;

              if(!f.exists()) {

                  Uri uri = Uri.parse(c.getMessage());
                  Log.d("File_Path",uri.toString());
                  Glide.with(context)
                          .asBitmap()
                          .load(uri)
                          .into(new SimpleTarget<Bitmap>() {
                              @Override
                              public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                  saveImage(resource);
                              }
                          });
              }
              else{
                  imageStorageDir.mkdirs();
              }

              view.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {

                      Intent imageFullScreen = new Intent(context,PhotoActivity.class);
                      imageFullScreen.putExtra("from","MessageAdapter");
                      imageFullScreen.putExtra("file",BigImage.getAbsolutePath());
                      context.startActivity(imageFullScreen);
                  }
              });
        }

        else if(message_type.equalsIgnoreCase("audio")){

            audioFile = new File(audioStorageDir,firebaseFileName);

            if(!audioFile.exists()) {
                ProgressDialog progressDialog = null;
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Downloading...");
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);

                final DownloadTask downloadTask = new DownloadTask(context, progressDialog, firebaseFileName, c.getType());
                downloadTask.execute(c.getMessage());
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
            }

            holder.messageText.setBackgroundResource(R.drawable.ic_audiotrack_black_24dp);

        }
        else if(message_type.equalsIgnoreCase("video")){

           videoFile = new File(videoStorageDir,firebaseFileName);

            if(!videoFile.exists()) {
                ProgressDialog progressDialog = null;
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Downloading...");
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);

                final DownloadTask downloadTask = new DownloadTask(context, progressDialog, firebaseFileName, c.getType());
                downloadTask.execute(c.getMessage());
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
            }
            holder.messageText.setBackgroundResource(R.drawable.ic_ondemand_video_black_24dp);
        }

        else if(message_type.equalsIgnoreCase("application")){

            docFile = new File(docStorageDir,firebaseFileName);

            if(!docFile.exists()) {
                ProgressDialog progressDialog = null;
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Downloading...");
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);

                final DownloadTask downloadTask = new DownloadTask(context, progressDialog, firebaseFileName, c.getType());
                downloadTask.execute(c.getMessage());
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
            }

            holder.messageText.setBackgroundResource(R.drawable.ic_picture_as_pdf_black_24dp);


        }

        else{

            Toast.makeText(context, "Sorry !!! Upload Type Is Not Supported !", Toast.LENGTH_SHORT).show();
        }




        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                CharSequence options[] = new CharSequence[]{"Delete for me", "Delete for All"," Play/View" ,"Share"};
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Select Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Click Event For Each Item

                        if(which == 0){

                            Toast.makeText(context, "Delete function is not set", Toast.LENGTH_SHORT).show();

                        }
                        else if(which == 1){
                            Toast.makeText(context, "Delete function is not set", Toast.LENGTH_SHORT).show();
                        }
                        else if(which == 2){


                            if(message_type.equalsIgnoreCase("image"))
                            {
                                Intent imageFullScreen = new Intent(context,PhotoActivity.class);
                                imageFullScreen.putExtra("from","MessageAdapter");
                                imageFullScreen.putExtra("file",BigImage.getAbsolutePath());
                                context.startActivity(imageFullScreen);
                            }
                           else if(message_type.equalsIgnoreCase("video")){

                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(videoFile),"video/*");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                Intent intent1 = Intent.createChooser(intent, "Open File");
                                try {

                                    context.startActivity(intent1);
                                }
                                catch(Exception e){
                                    Toast.makeText(context, "INSTALL THE VIDEO PLAYER", Toast.LENGTH_SHORT).show();
                                }

                            }

                           else if(message_type.equalsIgnoreCase("audio")){
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(audioFile),"audio/*");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                Intent intent1 = Intent.createChooser(intent, "Open File");
                                try {

                                    context.startActivity(intent1);
                                }
                                catch(Exception e){
                                    Toast.makeText(context, "INSTALL THE PDF READER", Toast.LENGTH_SHORT).show();
                                }


                            }
                            else if(message_type.equals("application")){
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(docFile),"application/*");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                Intent intent1 = Intent.createChooser(intent, "Open File");
                                try {

                                    context.startActivity(intent1);
                                }
                                catch(Exception e){
                                    Toast.makeText(context, "INSTALL THE PDF READER", Toast.LENGTH_SHORT).show();
                                }

                            }


                        }
                        else if(which == 3){
                            Toast.makeText(context, "Share is not set", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });

    }

    private  String firebaseFileName;
    private File imageStorageDir,videoStorageDir,audioStorageDir,docStorageDir;

    private String saveImage(Bitmap image) {

        String savedImagePath = null;
        String imageFileName = "JPEG_"+firebaseFileName;
        File storageDir = imageStorageDir;
        boolean success = true;
        if(!storageDir.exists()){
            success = storageDir.mkdirs();
        }

        if(success){
            File imageFile = new File(storageDir,imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try{

                OutputStream fout = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100 , fout);
                fout.close();
            }
            catch (Exception e){
                Log.d("ERROR_DOWNLOADING","ERROR WHILE DOWNLOADING IMAGE TO FILE");
            }
            galleryAddPic(savedImagePath);
            Toast.makeText(context, "IMAGE SAVED", Toast.LENGTH_LONG).show();
        }


        return savedImagePath;
    }

    private void galleryAddPic(String imagePath) {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f =  new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {


        public TextView messageText, messageTextExtended, lastSeen;//displayName;
        // public CircleImageView profileImage;
       // public ImageView messageImage;
        public LinearLayout textMsgWrapper;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
            messageTextExtended = (TextView) itemView.findViewById(R.id.textView27);
            //    profileImage = (CircleImageView)itemView.findViewById(R.id.message_profile_layout);
            lastSeen = (TextView) itemView.findViewById(R.id.message_single_layout_time);
            ;
            //    displayName=(TextView)itemView.findViewById(R.id.tv_message_single_layout_display_name);
          //  messageImage = (ImageView) itemView.findViewById(R.id.message_image);
            textMsgWrapper = (LinearLayout) itemView.findViewById(R.id.text_msg_wrapper);
        }
    }

    public String getName(String filepath){
        if(filepath == null || filepath.length() == 0){
            return "";
        }
        int extract = filepath.lastIndexOf('?');
        if(extract>0){
            filepath = filepath.substring(0,extract);
        }

        int namePos = filepath.lastIndexOf(File.separatorChar);
        return (namePos >=0 )? filepath.substring(namePos+1) :filepath;
    }

}


