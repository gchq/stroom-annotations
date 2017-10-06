import React, { Component } from 'react';
import PropTypes from 'prop-types'

import {red500 } from 'material-ui/styles/colors';
import IconButton from 'material-ui/IconButton';
import NotificationsIcon from 'material-ui/svg-icons/social/notifications';
import Popover from 'material-ui/Popover';
import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';

class ErrorDisplay extends Component {
    state = {
        open: false,
    };

    handleTouchTap = (event) => {
        // This prevents ghost click.
        event.preventDefault();

        this.setState({
            open: true,
            anchorEl: event.currentTarget,
        });
    };


    handleOpen(e){
        this.setState({
            open: true,
            anchorEl: e.currentTarget
        });
    };

    handleClose() {
        this.setState({open: false});
    };

    renderErrors() {
        if (this.props.errorMessages.length > 0) {
            return this.props.errorMessages.map(errorMessage => (
                <MenuItem key={errorMessage.id}
                    primaryText={errorMessage.action}
                    secondaryText={errorMessage.message}
                    onClick={() => this.props.acknowledgeError(errorMessage.id)}
                    />
            ))
        } else {
            return <MenuItem
                    primaryText='No new errors seen'
                    />
        }
    }

    render () {
        const iconColor = (this.props.errorMessages.length) ? red500 : undefined
        const menuStyle = {
            width: '30rem'
        }
        return (
            <div>
                <Popover
                    open={this.state.open}
                    anchorEl={this.state.anchorEl}
                    anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
                    targetOrigin={{horizontal: 'right', vertical: 'top'}}
                    onRequestClose={this.handleClose.bind(this)}
                    >

                    <Menu style={menuStyle}>
                        {this.renderErrors()}
                    </Menu>
                </Popover>

                <IconButton onClick={this.handleOpen.bind(this)}>
                    <NotificationsIcon color={iconColor} />
                </IconButton>
            </div>
        )
    }
}

ErrorDisplay.propTypes = {
    errorMessages: PropTypes.array.isRequired,

    acknowledgeError: PropTypes.func.isRequired
}

export default ErrorDisplay