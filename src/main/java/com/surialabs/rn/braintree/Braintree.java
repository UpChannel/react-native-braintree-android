package com.surialabs.rn.braintree;

import android.content.Intent;
import android.content.Context;
import android.app.Activity;

import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.BraintreePaymentActivity;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ActivityEventListener;


public class Braintree extends ReactContextBaseJavaModule implements ActivityEventListener {
  private static final int PAYMENT_REQUEST = 1;

  private Callback successCallback;
  private Callback errorCallback;

  public Activity activity;

  public Braintree(ReactApplicationContext reactContext) {
    super(reactContext);
    // reactContext.addLifecycleEventListener(this);
    reactContext.addActivityEventListener(this);
  }

  @Override
  public String getName() {
    return "Braintree";
  }

  // @Override
  // public void onHostResume() {
  //   Activity currentActivity = getCurrentActivity();
  //   if (currentActivity != null) {
  //     this.activity = currentActivity;
  //   }
  // }

  @ReactMethod
  public void paymentRequest(final String clientToken, final Callback successCallback, final Callback errorCallback) {
    this.successCallback = successCallback;
    this.errorCallback = errorCallback;

    Activity currentActivity = getCurrentActivity();

    PaymentRequest paymentRequest = new PaymentRequest()
    .clientToken(clientToken);

    if (currentActivity != null) {
      currentActivity.startActivityForResult(
        paymentRequest.getIntent(currentActivity),
        PAYMENT_REQUEST
      );
    }
    else {
      // try to spawn a new activity
      // getReactApplicationContext().startActivityForResult(
      //   paymentRequest.getIntent(getCurrentActivity()),
      //   PAYMENT_REQUEST
      // );
      this.errorCallback.invoke("Error starting payments service. Please retry.");
    }
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
    if (requestCode == PAYMENT_REQUEST) {
      switch (resultCode) {
        case Activity.RESULT_OK:
          PaymentMethodNonce paymentMethodNonce = intent.getParcelableExtra(
            BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE
          );
          this.successCallback.invoke(paymentMethodNonce.getNonce());
          break;
        case BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR:
        case BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR:
        case BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_UNAVAILABLE:
          this.errorCallback.invoke(
            intent.getSerializableExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE)
          );
          break;
        default:
          break;
      }
    }
  }
  
  // https://github.com/facebook/react-native/releases/tag/v0.30.0 (Native modules implementing ActivityEventListener now need to implement onNewIntent(Intent intent))
  public void onNewIntent(Intent intent) { }
}
