package me.oldboy.core.model.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;

import java.io.Serializable;

@RequiredArgsConstructor
public abstract class ServiceBase<K extends Serializable, T> {

    @Getter
    private final RepositoryBase<K, T> repositoryBase;
}
