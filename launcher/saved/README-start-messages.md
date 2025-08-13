# Chatroom Start Message Files

This directory contains pre-configured start message files for testing different game modes in the Chatroom module.

## Available Start Messages

### 1. `start-message-local-multiplayer.json`
- **Game Mode**: `local_multiplayer`
- **Players**: 4 players (Alice, Bob, Charlie, Diana)
- **Features**: 
  - Player selector dropdown
  - Local message handling
  - Server simulator for transcripts
  - Color-coded players

### 2. `start-message-single-player.json`
- **Game Mode**: `single_player`
- **Players**: 2 players (Human + AI)
- **Features**:
  - AI opponent with random 6-letter responses
  - 500ms response delay
  - Server simulator for transcripts
  - No player selector needed

### 3. `start-message-all-modes.json`
- **Game Mode**: `multi_player`
- **Players**: 2 players (Host + Guest)
- **Features**:
  - Traditional multiplayer with server communication
  - Server simulator for transcripts
  - Standard GDK behavior

## How to Use

1. **Copy the desired start message** from one of the JSON files
2. **Paste it into the GDK lobby** in the JSON input field
3. **Launch the Chatroom game** - it will automatically detect the game mode
4. **Test the functionality** based on the selected mode

## Game Mode Differences

### Local Multiplayer Mode
- Use the player selector dropdown to choose which player you're sending messages as
- Messages appear locally and are sent to server simulator for transcripts
- No actual network communication needed

### Single Player Mode
- Chat with an AI opponent that responds with random 6-letter messages
- AI responses have a 500ms delay to simulate thinking
- All messages are logged to server simulator for transcripts

### Multi Player Mode
- Traditional multiplayer with server communication
- Messages go through the server simulator
- Full transcript generation and logging

## Customization

You can modify these files to:
- Change player names and IDs
- Adjust AI response delays
- Modify game settings
- Add custom player attributes

## Testing Tips

- **Local Multiplayer**: Test the player selector by switching between different players
- **Single Player**: Verify AI responses are exactly 6 letters and have the correct delay
- **All Modes**: Check that server simulator shows all messages and generates transcripts 