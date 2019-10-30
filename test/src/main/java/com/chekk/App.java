package com.chekk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.PlacesApi;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.PlaceAutocompleteRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.DistanceMatrixElementStatus;;
/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     * @throws InterruptedException
     * @throws ApiException
     */
    public static void main(String[] args) throws IOException, ApiException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the first address");
        String first_address = reader.readLine();
        System.out.println(first_address);

        System.out.println("Enter the second address");
        String second_address = reader.readLine();
        System.out.println(second_address);

        String API_KEY = "ADD API KEY HERE";

        GeoApiContext context = new GeoApiContext.Builder().apiKey(API_KEY).retryTimeout(0, TimeUnit.MICROSECONDS).build();

        FindPlaceFromText address_1 = PlacesApi.findPlaceFromText(context,
                                                                    first_address, 
                                                                    FindPlaceFromTextRequest.InputType.TEXT_QUERY)
                                                                    .fields(        
                                                                        FindPlaceFromTextRequest.FieldMask.FORMATTED_ADDRESS,
                                                                        FindPlaceFromTextRequest.FieldMask.GEOMETRY,
                                                                        FindPlaceFromTextRequest.FieldMask.PLACE_ID)
                                                                    .await();

        FindPlaceFromText address_2 = PlacesApi.findPlaceFromText(context,
                                                                    second_address, 
                                                                    FindPlaceFromTextRequest.InputType.TEXT_QUERY)
                                                                    .fields(        
                                                                        FindPlaceFromTextRequest.FieldMask.FORMATTED_ADDRESS,
                                                                        FindPlaceFromTextRequest.FieldMask.GEOMETRY,
                                                                        FindPlaceFromTextRequest.FieldMask.PLACE_ID)
                                                                    .await();

        String first_address_id = address_1.candidates[0].placeId;
        String second_address_id = address_2.candidates[0].placeId;

        PlaceDetailsRequest address_1_req = PlacesApi.placeDetails(context,
                                                                    first_address_id);
        PlaceDetails addressDetails_1 = address_1_req.await();

        PlaceDetailsRequest address_2_req = PlacesApi.placeDetails(context,
                                                                    second_address_id);
        PlaceDetails addressDetails_2 = address_2_req.await();

        boolean coord = false;
        boolean city = false;
        boolean identical = false;
        boolean post_code = false;
        boolean road = false;
        

        if(addressDetails_1.geometry.location.equals(addressDetails_2.geometry.location)){
            System.out.println("Addresses have the same Google Maps coordinates.");
            coord = true;
            city = true;
            identical = true;
            post_code = true;
            road = true;
        }

        String add_1_city = new String();
        String add_1_post_code = new String();
        String add_2_city = new String();
        String add_2_post_code = new String();
        String add_1_route = new String();
        String add_2_route = new String();

        for (int i =0; i<addressDetails_1.addressComponents.length; i++) {
            if(addressDetails_1.addressComponents[i].types[0].equals(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1)){
                add_1_city = addressDetails_1.addressComponents[i].longName;
                
            }

            if(addressDetails_1.addressComponents[i].types[0].equals(AddressComponentType.POSTAL_CODE)){
                add_1_post_code = addressDetails_1.addressComponents[i].longName;
            }

            if(addressDetails_1.addressComponents[i].types[0].equals(AddressComponentType.ROUTE)){
                add_1_route = addressDetails_1.addressComponents[i].longName;
            }
        }

        for (int i =0; i<addressDetails_2.addressComponents.length; i++) {
            if(addressDetails_2.addressComponents[i].types[0].equals(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1)){
                add_2_city = addressDetails_2.addressComponents[i].longName;
            }

            if(addressDetails_2.addressComponents[i].types[0].equals(AddressComponentType.POSTAL_CODE)){
                add_2_post_code = addressDetails_2.addressComponents[i].longName;
            }

            if(addressDetails_2.addressComponents[i].types[0].equals(AddressComponentType.ROUTE)){
                add_2_route = addressDetails_2.addressComponents[i].longName;
            }
        }

        if(add_1_city.equals(add_2_city)){
            System.out.println("Addresses are in the same city");
            city = true;
        }

        if(add_1_post_code.equals(add_2_post_code)){
            System.out.println("Addresses have the same postal code");
            post_code = true;
        }

        if(add_1_route.equals(add_2_route)){
            System.out.println("Addresses are on the same road");
            road = true;
        }


        //checking the distance        
        DistanceMatrix distance = DistanceMatrixApi.getDistanceMatrix(context
                                                                    ,new String[] {first_address}
                                                                    ,new String[] {second_address})
                                                                    .await();

        if(distance.rows[0].elements[0].status.equals(DistanceMatrixElementStatus.OK)){
            if(distance.rows[0].elements[0].distance.inMeters <= 1000){
                System.out.println("Adresses are almost identical (less than 1 km of distance)");
                identical = true;
            }
        }

    }
}
