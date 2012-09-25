package com.gruter.drone.service;

import com.gruter.drone.model.QueryTask;

public interface IQueryFinishListener {
  public void finishedQuery(QueryTask queryTask);
}
