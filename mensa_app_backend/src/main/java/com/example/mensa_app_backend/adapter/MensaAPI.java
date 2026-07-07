package com.example.mensa_app_backend.adapter;

import java.util.List;

import com.example.mensa_app_backend.menu.MenuItem;

public interface MensaAPI{
    boolean authenticate(int nameHash, int pwHash);
    //eigentlich wollte ich Arraylist benutzen aber der Code is jetzt List basiert
    List<MenuItem> getAllMenuItems() ;
    List<MenuItem> getFilteredMenuItems(boolean... preferences);
}
