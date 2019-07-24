package com.pnrhunter.mvp.authorization;


import android.os.Bundle;


public class RegistrationViewTest
        extends RegistrationView{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.nameInput.setText("Abstract");
        super.lastNameInput.setText("Human");
        super.emailInput.setText("alex.alex2825@gmail.com");
        super.passwordInput.setText("1aaaaaaaa");
        super.passwordConfirmationInput.setText("1aaaaaaaa");
    }

}