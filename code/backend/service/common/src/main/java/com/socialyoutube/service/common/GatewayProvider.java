package com.socialyoutube.service.common;

import com.duspensky.jutils.common.ThreadExecutor;

import com.duspensky.jutils.common.CloseableHolder;
import com.duspensky.jutils.rmqrmi.BaseSerializer;
import com.duspensky.jutils.rmqrmi.Gateway;
import com.duspensky.jutils.rmqrmi.GatewayBuilder;

import org.apache.commons.lang3.ArrayUtils;

public class GatewayProvider implements AutoCloseable {
  private ThreadExecutor executor;
  private Gateway gateway;

  private static class DummySerializer extends BaseSerializer {
    @Override
    public byte[] serialize(Object[] objs) {
      return new byte[0];
    }
  
    @Override
    public Object[] deserialize(Class<?>[] cls, byte[] data) {
      return ArrayUtils.toArray();
    }
  
  }

  private GatewayProvider() throws Exception {
    try (CloseableHolder<ThreadExecutor> exec = new CloseableHolder<>(new ThreadExecutor())){
      gateway = new GatewayBuilder().setExecutor(exec.get()).setSerializer(new DummySerializer()).build();
      executor = exec.release();
    }
  }

  public Gateway gateway() {
    return gateway;
  }

  @Override
  public void close() throws Exception {
    executor.close();
  }
}
