export interface IConvivaData {
  customerKey: string | undefined;
  debug: boolean;
  debugProxyUrl: string | null;
  isLive: boolean;
  playerName?: string;
  playerVersion?: string;
  title: string | null;
  url: string;
  viewerId: string;
}