package com.dhy.hotfix.updater;


import java.util.List;

import io.reactivex.Observable;

public interface IPatchVersionApi {
    Observable<IPatchVersion> checkPatchVersion();

    Observable<List<PatchUser>> fetchPatchUsers();
}
