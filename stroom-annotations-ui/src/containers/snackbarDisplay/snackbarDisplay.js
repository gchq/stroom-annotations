import React, { Component } from 'react'
import PropTypes from 'prop-types'

import Snackbar from 'material-ui/Snackbar';

export class SnackbarDisplay extends Component {

    handleRequestClose = (messageId) => {
        this.props.acknowledgeSnackbar(messageId)
    };

    snackbarOpen() {
        return this.props.messages.length > 0
    }

    snackbarMessage() {
        if (this.props.messages.length > 0) {
            return this.props.messages[0].message
        } else {
            return ''
        }
    }

    onRequestClose() {
        if (this.props.messages.length > 0) {
            this.handleRequestClose(this.props.messages[0].id)
        }
    }

    render() {
        return (
            <div>
                <Snackbar
                    open={this.snackbarOpen()}
                    message={this.snackbarMessage()}
                    autoHideDuration={4000}
                    onRequestClose={this.onRequestClose.bind(this)}
                    />
            </div>
        )
    }
}

SnackbarDisplay.propTypes = {
    messages: PropTypes.array.isRequired,
    acknowledgeSnackbar: PropTypes.func.isRequired
}

export default SnackbarDisplay