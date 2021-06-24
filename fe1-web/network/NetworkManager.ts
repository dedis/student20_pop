import { JsonRpcRequest, JsonRpcResponse } from 'model/network';
import { NetworkConnection } from './NetworkConnection';
import { NetworkError } from './NetworkError';
import { defaultRpcHandler, JsonRpcHandler } from './RpcHandler';

let NETWORK_MANAGER_INSTANCE: NetworkManager;

class NetworkManager {
  private connections: NetworkConnection[];

  private rpcHandler: JsonRpcHandler = defaultRpcHandler;

  public constructor() {
    this.connections = [];
  }

  private static buildAddress(host: string, port: number, path: string): string {
    const base = `ws://${host}:${port}`;
    return (path === '')
      ? base
      : `${base}/${path}`;
  }

  private getConnectionByAddress(address: string): NetworkConnection | undefined {
    return this.connections.find((nc: NetworkConnection) => nc.address === address);
  }

  /** Connects to a server or returns an existing connection to the server
   * Mockserver port: 8080
   * Go backend default organizer port: 9000
   * The full path to connect to the backend is:
   * as organizer ws://host:clientport/organizer/client/
   * as a witness: ws://host:witnessport/organizer/witness/
   * @param host the server's host
   * @param port the server's port
   * @param path the path at which the websocket can be established
   *
   * @returns a new connection to the server, or an existing one if it's already established
   */
  public connect(host: string, port: number = 9000, path: string = 'organizer/client/'): NetworkConnection {
    const address: string = NetworkManager.buildAddress(host, port, path);
    // Here the demo url is hardcoded which was used during the final demo of the spring semester 21
    // const address = 'wss://popdemo.dedis.ch/demo';

    const existingConnection = this.getConnectionByAddress(address);

    if (existingConnection !== undefined) {
      return existingConnection;
    }

    const connection: NetworkConnection = new NetworkConnection(address, this.rpcHandler);
    this.connections.push(connection);
    return connection;
  }

  public disconnect(connection: NetworkConnection): void {
    const index = this.connections.indexOf(connection);
    if (index !== -1) {
      this.connections[index].disconnect();
      this.connections.splice(index, 1);
    }
  }

  public disconnectFrom(host: string, port: number = 8080, path: string = ''): void {
    const address = NetworkManager.buildAddress(host, port, path);
    const connection = this.getConnectionByAddress(address);
    if (connection !== undefined) {
      this.disconnect(connection);
    }
  }

  public disconnectFromAll(): void {
    this.connections.forEach((nc: NetworkConnection) => nc.disconnect());
    this.connections = [];
  }

  /** Sends a JsonRpcRequest over the network to any relevant server.
   *
   * @param payload The JsonRpcRequest you want to send
   *
   * @returns a Promise being resolved with the response,
   * or rejected with an error if the payload could not be delivered or was rejected by the server
   */
  public sendPayload(payload: JsonRpcRequest): Promise<JsonRpcResponse> {
    // For now, we only have 1 connection opened at a time: the organizer server

    if (this.connections.length === 0) {
      return Promise.reject(new NetworkError('Cannot send payload:'
        + ' no websocket connection available'));
    }

    return this.connections[0].sendPayload(payload)
      .catch((error) => {
        // Some day, we will want to retry from a different network connection,
        // before throwing an error to the caller
        console.error('Could not send payload due to failure:', error);
        throw error;
      });
  }

  public setRpcHandler(handler: JsonRpcHandler): void {
    this.rpcHandler = handler;
    this.connections.forEach((c) => c.setRpcHandler(handler));
  }
}

export function getNetworkManager(): NetworkManager {
  if (NETWORK_MANAGER_INSTANCE === undefined) {
    NETWORK_MANAGER_INSTANCE = new NetworkManager();
  }
  return NETWORK_MANAGER_INSTANCE;
}
