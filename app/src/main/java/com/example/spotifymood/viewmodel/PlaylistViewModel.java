package com.example.spotifymood.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spotifymood.network.PlaylistItemsDto;
import com.example.spotifymood.repository.PlaylistRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistViewModel extends AndroidViewModel {
    private final PlaylistRepository repo = new PlaylistRepository();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private final MutableLiveData<PlaylistItemsDto> itemsLive = new MutableLiveData<>();
    private final MutableLiveData<String> errorLive = new MutableLiveData<>();

    public PlaylistViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<PlaylistItemsDto> items() { return itemsLive; }
    public LiveData<String> error() { return errorLive; }

    public void loadPlaylistItems(String playlistId) {
        io.execute(() -> {
            try {
                PlaylistItemsDto dto = repo.getPlaylistItems(getApplication(), playlistId, 50, 0);
                if (dto != null) itemsLive.postValue(dto);
                else errorLive.postValue("Failed to load tracks");
            } catch (Exception e) {
                errorLive.postValue(e.getMessage());
            }
        });
    }
}
