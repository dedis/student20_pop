import { Hash } from './Hash';

export type Channel = string;
export const ROOT_CHANNEL: Channel = '/root';

export function channelFromId(value?: Hash) : Channel {
  if (value === undefined) return ROOT_CHANNEL;

  const ch = value.valueOf();
  return `${ROOT_CHANNEL}/${ch}`;
}

export function channelFromIds(...args: Hash[]): Channel {
  let channel = ROOT_CHANNEL;
  args.forEach((ch) => { channel += `/${ch.valueOf()}`; });
  return channel;
}

/** Returns the last part of the channel which is usually an event id
 * Example:
 * Input: /root/laoid/electionid
 * Output: electionId
 */
export function getLastChannel(channel: Channel): Hash {
  const channels = channel.split('/');
  return new Hash(channels[channels.length - 1]);
}
