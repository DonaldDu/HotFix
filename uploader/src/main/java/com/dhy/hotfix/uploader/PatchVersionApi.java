package com.dhy.hotfix.uploader;


import java.util.List;

import io.reactivex.Observable;

public interface PatchVersionApi {
    Observable<IVersion> checkPatchVersion();

    Observable<List<PatchUser>> fetchPatchUsers();
}
