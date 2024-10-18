package com.aluracursos.screenmatch.service;

public interface IConviertedatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
