package com.socialyoutube.service.common;

import com.duspensky.jutils.rmi.Exceptions.BadSerialization;
import com.duspensky.jutils.rmi.BaseSerializer;
import com.duspensky.jutils.rmi.Gateway;
import com.duspensky.jutils.rmi.GatewayFactory;
import com.duspensky.jutils.rmq.TransportFactoryRMQ;

public class GatewayProvider implements AutoCloseable {
  private Gateway gateway;
  private static class DummySerializer extends BaseSerializer {

    @Override
    public Object[] deserialize(Class<?>[] arg0, byte[] arg1) throws BadSerialization {
      return null;
    }

    @Override
    public byte[] serialize(Object[] arg0) throws BadSerialization {
      return null;
    }

  }

  public GatewayProvider() throws Exception {
    gateway = new GatewayFactory().build(new TransportFactoryRMQ(), new DummySerializer(), null, null, 30000);
  }

  public Gateway getGateway() {
    return gateway;
  }

  @Override
  public void close() throws Exception {
    gateway.close();
  }
}
