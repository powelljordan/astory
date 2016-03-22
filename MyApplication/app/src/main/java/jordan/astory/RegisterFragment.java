package jordan.astory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Jordan on 1/8/2016.
 */
public class RegisterFragment extends DialogFragment {
    private ViewHolder viewHolder;
    RegisterFragmentListener activity;
    EditText usernameEditText;
    EditText emailEditText;
    EditText passwordEditText;

    RegisterFragmentListener listener;

    public interface RegisterFragmentListener{
        void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog);
        void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog);
        void onFinishedInputDialog(String username, String email, String password);
    }

    public void setListener(RegisterFragmentListener listener) {
        this.listener = listener;
    }

    private ViewHolder getViewHolder() {
        return viewHolder;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.register_dialog, null);
        viewHolder = new ViewHolder();


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.registration_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id){

                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RegisterFragment.this.getDialog().cancel();

                        if (listener != null) {
                            listener.onDialogNegativeClick(RegisterFragment.this);
                        }
                    }
                });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onDialogPositiveClick(RegisterFragment.this);
                            Dialog d = (Dialog) dialog;
                            usernameEditText = (EditText) d.findViewById(R.id.fragment_username);
                            emailEditText = (EditText) d.findViewById(R.id.fragment_email);
                            passwordEditText = (EditText) d.findViewById(R.id.fragment_password);
                            activity = (RegisterFragmentListener) getActivity();
                            EditText userNameInput = usernameEditText;
                            String username = usernameEditText.getText().toString();
                            String email = emailEditText.getText().toString();
                            String password = passwordEditText.getText().toString();
                            if(dataIsValid(username, email)){
                                activity.onFinishedInputDialog(username, email, password);
                                dialog.dismiss();
                            }
                            else{
                                Toast.makeText(getContext(), "Please enter text into both fields", Toast.LENGTH_SHORT).show();
                            }

//                            EditText searchTo = (EditText)d.findViewById(R.id.fragment_email);

                        }
                    }

                });

            }
        });


        return dialog;
    }

    public boolean dataIsValid(String username, String email){
        if(!username.equals("") && !email.equals("")){
            return true;
        }
        return false;
    }

    public class ViewHolder {
        public void populate(View v) {
//            usernameEditText = (EditText) v.findViewById(R.id.fragment_username);
            emailEditText = (EditText)v.findViewById(R.id.fragment_email);
            passwordEditText = (EditText)v.findViewById(R.id.fragment_password);
        }
    }
}
