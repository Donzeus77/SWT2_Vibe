package com.example.mensa_app_backend.adapter;

import java.util.List;

import com.example.mensa_app_backend.menu.MenuItem;

public class MensaAdapter implements MensaAPI{

    @Override
    public boolean authenticate(int nameHash, int pwHash) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MenuItem> getFilteredMenuItems(boolean... preferences) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
