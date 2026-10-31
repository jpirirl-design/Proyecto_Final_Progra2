/* Usado por scripts/inicializar-docker.ps1: falla si ya hay equipos registrados. */
IF EXISTS (SELECT 1 FROM SIGELAB.dbo.Equipo)
    THROW 51000, 'DATOS_EXISTENTES', 1;
