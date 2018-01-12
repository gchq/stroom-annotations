import React, { Component } from 'react'
import PropTypes from 'prop-types'

import SimpleSchema from 'simpl-schema'

import RaisedButton from 'material-ui/RaisedButton'
import FlatButton from 'material-ui/FlatButton'
import Dialog from 'material-ui/Dialog'
import TextField from 'material-ui/TextField'
import Add from 'material-ui-icons/Add'
import { fullWhite } from 'material-ui/styles/colors'

class CreateAnnotation extends Component {
    state = {
        createDialogOpen: false,
        newAnnotationId: '',
        newAnnotationIdError: undefined
    };

    onNewAnnotationIdChange(e) {
        this.setState({
            newAnnotationId: e.target.value
        })
    }

    // Create dialog
    handleCreateDialogOpen() {
        this.setState({
            createDialogOpen: true
        });
    };

    handleCreateDialogClose() {
        this.setState({
            createDialogOpen: false,
            newAnnotationId: ''
        });
    };

    handleConfirmCreate() {

        try {
            // Validate the new ID
            new SimpleSchema({
                id: {
                    type: String,
                    required: true,
                    min: 3
                }
            }).validate({
                id: this.state.newAnnotationId,
            });

            // Clear any previous error
            this.setState({
                newAnnotationIdError: undefined
            })

            this.props.createAnnotation(this.props.indexUuid, this.state.newAnnotationId)
            this.handleCreateDialogClose()
        } catch(e) {
            this.setState({
                newAnnotationIdError: e.message
            })
        }
    }

    render() {
        const createDialogActions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleCreateDialogClose.bind(this)}
                />,
            <FlatButton
                label="Create"
                primary={true}
                onClick={this.handleConfirmCreate.bind(this)}
                />,
        ];

        return (
            <div>
                <Dialog
                    title='Create a new Annotation'
                    actions={createDialogActions}
                    modal={false}
                    open={this.state.createDialogOpen}
                    onRequestClose={this.handleCreateDialogClose.bind(this)}
                    contentClassName='dialog'
                    >
                    <TextField value={this.state.newAnnotationId} onChange={this.onNewAnnotationIdChange.bind(this)}
                        hintText="Enter the ID for the new annotation"
                        floatingLabelText="Annotation ID"
                        fullWidth={true}
                        errorText={this.state.newAnnotationIdError}
                        />
                </Dialog>

                <RaisedButton
                    label="Create"
                    primary={true}
                    icon={<Add color={fullWhite} />}
                    className='toolbar-button-small'
                    onClick={this.handleCreateDialogOpen.bind(this)}
                    />
            </div>
        )
    }
}

CreateAnnotation.propTypes = {
    indexUuid: PropTypes.string.isRequired,
    createAnnotation: PropTypes.func.isRequired
}

export default CreateAnnotation
