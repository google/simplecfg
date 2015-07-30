/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.shipshape.extendj;

import com.google.shipshape.util.rpc.HttpServerFrontend;
import com.google.shipshape.util.rpc.Server;
import com.google.shipshape.util.rpc.HttpServerFrontend;
import com.google.shipshape.util.rpc.Server;
import com.google.shipshape.proto.ShipshapeContextProto.Stage;
import com.google.shipshape.service.Analyzer;
import com.google.shipshape.service.JavaDispatcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.ArrayList;

/** Run ExtendJ based analyzers. */
class ExtendJService {

  @Parameter(names = "--port", description = "port for the analyzer RPC server")
  private int port = 10008;

  public static void main(String[] args) throws Throwable {
    try {
      ExtendJService service = new ExtendJService();
      new JCommander(service, args);

      ArrayList<Analyzer> analyzers = new ArrayList<>();
      analyzers.add(new ExtendJAnalyzer());

      Server server = new Server();
      JavaDispatcher<Object> dispatcher = new JavaDispatcher<>(analyzers, Stage.PRE_BUILD, null);
      dispatcher.register(server);
      System.out.format("Starting ExtendJ service at %d\n", service.port);
      new HttpServerFrontend(server, service.port).run();
    } catch (Throwable t) {
      System.err.println("Error starting service");
      throw t;
    }
  }
}
