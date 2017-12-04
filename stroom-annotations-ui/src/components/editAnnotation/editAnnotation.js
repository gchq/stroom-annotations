import React, { Component } from 'react'
import PropTypes from 'prop-types'

import './EditAnnotation.css'

import Dialog from 'material-ui/Dialog'
import FlatButton from 'material-ui/FlatButton'
import RaisedButton from 'material-ui/RaisedButton'
import TextField from 'material-ui/TextField'

import SelectStatus from '../selectStatus'

class EditAnnotation extends Component {

    constructor(props) {
        super(props);

        this.state = {
            open: false,
        };
    }

    saveChanges() {
        this.props.updateAnnotation(this.props.indexUuid, this.props.annotationId, this.props.annotation);
    }

    handleOpen() {
        this.setState({open: true});
    };

    handleClose() {
        this.setState({open: false});
    };

    handleRemoveAndClose() {
        this.props.removeAnnotation(this.props.indexUuid, this.props.annotationId)
        this.handleClose();
    }

    onAssignToChange(e) {
        const updates = {
            assignTo: e.target.value
        }

        this.props.editAnnotation(this.props.annotationId, updates)
    }

    onContentChange(e) {
        const updates = {
            content: e.target.value
        }

        this.props.editAnnotation(this.props.annotationId, updates)
    }

    onStatusChange(status) {
        this.props.editAnnotation(this.props.annotationId, { status })
    }

    handleViewHistory() {
        if (this.props.allowNavigation) {
            this.props.history.push(`/historyWithNav/${this.props.indexUuid}/${this.props.annotationId}`)
        } else {
            this.props.history.push(`/history/${this.props.indexUuid}/${this.props.annotationId}`)
        }
    }

    render() {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleClose.bind(this)}
                />,
            <FlatButton
                label="Remove"
                primary={true}
                onClick={this.handleRemoveAndClose.bind(this)}
                />,
        ];

        return (
            <div className='edit-annotation'>
                <TextField value={this.props.annotation.content}
                        onChange={this.onContentChange.bind(this)}
                        hintText="Write notes against this event"
                        floatingLabelText="Annotation Content"
                        multiLine={true}
                        rows={1}
                        rowsMax={4}
                        fullWidth={true}
                    />

                <TextField value={this.props.annotation.assignTo}
                        onChange={this.onAssignToChange.bind(this)}
                        hintText="Enter the name/identifier of someone to assign this to"
                        floatingLabelText="Assign To"
                        fullWidth={true}
                    />

                <SelectStatus
                        value={this.props.annotation.status}
                        onChange={this.onStatusChange.bind(this)}
                        />

                <div>
                    <RaisedButton
                            label="Save Changes"
                            onClick={this.saveChanges.bind(this)}
                            primary={true}
                            className='edit-annotation__save-button'
                            />
                    <RaisedButton
                            label="Remove Annotation"
                            onClick={this.handleOpen.bind(this)}
                            className='edit-annotation__remove-button'
                            />
                    <RaisedButton
                        label="View History"
                        primary={true}
                        onClick={this.handleViewHistory.bind(this)}
                        />
                </div>

                <Dialog
                  actions={actions}
                  modal={false}
                  open={this.state.open}
                  onRequestClose={this.handleClose.bind(this)}
                >
                    Remove the Annotation for event {this.props.annotationId}?
                </Dialog>

            </div>
        )
    }
}

EditAnnotation.propTypes = {
    history: PropTypes.object.isRequired,

    indexUuid: PropTypes.string.isRequired,
    annotationId: PropTypes.string.isRequired,
    annotation: PropTypes.object.isRequired,
    allowNavigation: PropTypes.bool.isRequired,

    editAnnotation: PropTypes.func.isRequired,
    updateAnnotation: PropTypes.func.isRequired,
    removeAnnotation: PropTypes.func.isRequired,
}

export default EditAnnotation