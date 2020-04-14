package ng.com.laundrify.laundrify.RaveClasses.data;



import java.util.List;

import ng.com.laundrify.laundrify.RaveClasses.FeeCheckRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.card.ChargeRequestBody;
import ng.com.laundrify.laundrify.RaveClasses.card.TokenChargeBody;
import ng.com.laundrify.laundrify.RaveClasses.responses.FeeCheckResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by hamzafetuga on 18/07/2017.
 */

public interface ApiService {

    //For first Card Charge
    @POST("/flwv3-pug/getpaidx/api/charge")
//    Call<ChargeResponse> charge(@Body ChargeRequestBody body);
    Call<String> charge(@Body ChargeRequestBody body);

    //For second Card Authentication and Third Validification
    @POST("/flwv3-pug/getpaidx/api/validatecharge")
    Call<String> validateCardCharge(@Body ValidateChargeBody body);
//    Call<ChargeResponse> validateCardCharge(@Body ValidateChargeBody body);

    //For third BAnk Authentication
    @POST("/flwv3-pug/getpaidx/api/validate")
    Call<String> validateAccountCharge(@Body ValidateChargeBody body);
//    Call<ChargeResponse> validateAccountCharge(@Body ValidateChargeBody body);

    @POST("/flwv3-pug/getpaidx/api/verify/mpesa")
    //@POST("/flwv3-pug/getpaidx/api/v2/verify")
    Call<String> requeryTx(@Body RequeryRequestBody body);
//    Call<RequeryResponse> requeryTx(@Body RequeryRequestBody body);

    @POST("/flwv3-pug/getpaidx/api/v2/verify")
    Call<String> requeryTx_v2(@Body RequeryRequestBodyv2 body);


    //Get Banks Availiabe
    @GET("/flwv3-pug/getpaidx/api/flwpbf-banks.js?json=1'")
    Call<List<Bank>> getBanks();

    //For Card Tokenized charge
    @POST("/flwv3-pug/getpaidx/api/tokenized/charge")
    Call<String> chargeToken(@Body TokenChargeBody payload);

    @POST("/flwv3-pug/getpaidx/api/fee")
    Call<FeeCheckResponse> checkFee(@Body FeeCheckRequestBody body);

}
